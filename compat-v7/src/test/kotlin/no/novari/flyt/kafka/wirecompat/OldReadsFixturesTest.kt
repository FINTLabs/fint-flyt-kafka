package no.novari.flyt.kafka.wirecompat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.topic.name.ErrorEventTopicNameParameters
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestConstructor
import org.springframework.util.backoff.FixedBackOff
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * fint-flyt-kafka 7 reads every fixture in `wire-compat/fixtures`, from all versions, with the classes the flyt apps
 * use. This is what makes a rollback to fint-flyt-kafka 7 safe.
 */
@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OldReadsFixturesTest(
    private val listenerFactoryService: InstanceFlowListenerFactoryService,
    embeddedKafka: EmbeddedKafkaBroker,
) {
    private val producer =
        KafkaProducer(
            mapOf<String, Any>(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to embeddedKafka.brokersAsString),
            ByteArraySerializer(),
            ByteArraySerializer(),
        )

    /** One listener per topic and consumer class, each in its own group, so each gets every record on the topic. */
    private val containers = ConcurrentHashMap<Pair<String, Class<*>>, ConcurrentMessageListenerContainer<String, *>>()

    /** Consumed records and failures, by fixture marker and consumer class. */
    private val consumed = ConcurrentHashMap<Pair<String, Class<*>>, CompletableFuture<InstanceFlowConsumerRecord<*>>>()

    @TestFactory
    fun `fint-flyt-kafka 7 reads the fixtures`(): List<DynamicTest> {
        val fixtures = WireCompatFixtures.all()
        assertFalse(fixtures.isEmpty(), "no fixtures in ${WireCompatFixtures.directory}")
        return fixtures.flatMap { (version, file) ->
            val fixture: FixtureRecord = fixtureMapper.readValue(file)
            val case = WireCompatCases.byName(fixture.case)
            val marker = "$version/${fixture.case}"
            case.consumers.map { consumer ->
                dynamicTest("$marker as ${consumer.type.simpleName}") {
                    val result = future(marker, consumer.type)
                    container(case, consumer.type)
                    if (consumer == case.consumers.first()) publish(fixture, marker)
                    assertConsumed(case, consumer, result.get(15, TimeUnit.SECONDS))
                }
            }
        }
    }

    @AfterAll
    fun stop() {
        containers.values.forEach { it.stop() }
        producer.close()
    }

    private fun assertConsumed(
        case: WireCompatCase,
        consumer: Consumer,
        record: InstanceFlowConsumerRecord<*>,
    ) {
        assertEquals(case.instanceFlowHeaders, record.instanceFlowHeaders, "instance flow headers")
        if (consumer.expect != Expect.Readable) {
            assertEquals(case.expected(consumer), record.consumerRecord.value(), "value")
        }
        val headers = record.consumerRecord.headers()
        case.additionalHeaders.forEach { (key, value) ->
            assertEquals(value, headers.lastHeader(key)?.value()?.toString(UTF_8), "header $key")
        }
        // fint-flyt-history-service requires it.
        assertNotNull(headers.lastHeader(WireCompatCases.ORIGIN_APPLICATION_ID_HEADER), "origin.application.id")
    }

    private fun publish(
        fixture: FixtureRecord,
        marker: String,
    ) {
        val headers =
            fixture.headers.map { RecordHeader(it.key, it.value.toByteArray(UTF_8)) } +
                RecordHeader(MARKER_HEADER, marker.toByteArray(UTF_8))
        producer
            .send(
                ProducerRecord(
                    fixture.topic,
                    null,
                    fixture.key?.toByteArray(UTF_8),
                    fixture.value?.toByteArray(UTF_8),
                    headers,
                ),
            ).get(10, TimeUnit.SECONDS)
    }

    private fun future(
        marker: String,
        type: Class<*>,
    ) = consumed.computeIfAbsent(marker to type) { CompletableFuture() }

    private fun container(
        case: WireCompatCase,
        type: Class<*>,
    ) = containers.computeIfAbsent(case.topic to type) {
        @Suppress("UNCHECKED_CAST")
        val valueClass = type as Class<Any>
        listenerFactoryService
            .createRecordListenerContainerFactory(
                valueClass,
                { record -> future(marker(record), type).complete(record) },
                ListenerConfiguration
                    .stepBuilder()
                    .groupIdApplicationDefaultWithSuffix("-${case.topic}-${type.simpleName}")
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .continueFromPreviousOffsetOnAssignment()
                    .build(),
                DefaultErrorHandler(
                    { record, exception ->
                        val marker =
                            record
                                .headers()
                                .lastHeader(MARKER_HEADER)
                                ?.value()
                                ?.toString(UTF_8)
                        if (marker != null) future(marker, type).completeExceptionally(exception)
                    },
                    FixedBackOff(0, 0),
                ),
            ).createContainer(topicNameParameters(case))
            .also { it.start() }
    }

    private fun marker(record: InstanceFlowConsumerRecord<*>): String =
        requireNotNull(record.consumerRecord.headers().lastHeader(MARKER_HEADER)).value().toString(UTF_8)

    private fun topicNameParameters(case: WireCompatCase): TopicNameParameters {
        val prefix =
            TopicNamePrefixParameters
                .stepBuilder()
                .orgId(WireCompatCases.ORG_ID)
                .domainContext(WireCompatCases.DOMAIN_CONTEXT)
                .build()
        return when (case.topicType) {
            TopicType.EVENT -> {
                EventTopicNameParameters
                    .builder()
                    .topicNamePrefixParameters(prefix)
                    .eventName(case.eventName)
                    .build()
            }

            TopicType.ERROR_EVENT -> {
                ErrorEventTopicNameParameters
                    .builder()
                    .topicNamePrefixParameters(prefix)
                    .errorEventName(case.eventName)
                    .build()
            }
        }
    }

    private companion object {
        /** Added by the test so it can tell the records apart. fint-flyt-kafka ignores unknown headers. */
        const val MARKER_HEADER = "wire-compat.fixture"

        val fixtureMapper = jacksonObjectMapper()
    }
}
