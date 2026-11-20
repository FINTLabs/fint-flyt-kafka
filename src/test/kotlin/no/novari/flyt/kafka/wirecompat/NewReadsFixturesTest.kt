package no.novari.flyt.kafka.wirecompat

import no.novari.flyt.kafka.instanceflow.headers.INSTANCE_FLOW_HEADERS_KEY
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.model.ErrorCollection
import no.novari.flyt.kafka.model.InstanceErrorEvent
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
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestConstructor
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * This version reads every fixture in `wire-compat/fixtures`, from all versions, the way the flyt apps read records:
 * `@KafkaListener` with the value as a parameter and `@Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders`.
 */
@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class NewReadsFixturesTest(
    private val listeners: FixtureListeners,
    embeddedKafka: EmbeddedKafkaBroker,
) {
    private val producer =
        KafkaProducer(
            mapOf<String, Any>(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to embeddedKafka.brokersAsString),
            ByteArraySerializer(),
            ByteArraySerializer(),
        )

    @TestFactory
    fun `this version reads the fixtures`(): List<DynamicTest> {
        val fixtures = WireCompatFixtures.all()
        assertFalse(fixtures.isEmpty(), "no fixtures in ${WireCompatFixtures.directory}")
        return fixtures.flatMap { (version, file) ->
            val fixture: FixtureRecord = fixtureMapper.readValue(file)
            val case = WireCompatCases.byName(fixture.case)
            val marker = "$version/${fixture.case}"
            case.consumers.map { consumer ->
                dynamicTest("$marker as ${consumer.type.simpleName}") {
                    if (consumer == case.consumers.first()) publish(fixture, marker)
                    val consumed = listeners.future(marker, consumer.type).get(15, TimeUnit.SECONDS)
                    assertEquals(case.instanceFlowHeaders, consumed.instanceFlowHeaders, "instance flow headers")
                    if (consumer.expect != Expect.Readable) {
                        assertEquals(case.expected(consumer), consumed.value, "value")
                    }
                    case.additionalHeaders.forEach { (key, value) ->
                        assertEquals(value, consumed.header(key), "header $key")
                    }
                    // fint-flyt-history-service requires it.
                    assertNotNull(
                        consumed.header(WireCompatCases.ORIGIN_APPLICATION_ID_HEADER),
                        "origin.application.id",
                    )
                }
            }
        }
    }

    @AfterAll
    fun stop() {
        producer.close()
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
                ProducerRecord<ByteArray?, ByteArray?>(
                    fixture.topic,
                    null as Int?,
                    fixture.key?.toByteArray(UTF_8),
                    fixture.value?.toByteArray(UTF_8),
                    headers,
                ),
            ).get(10, TimeUnit.SECONDS)
    }

    data class Consumed(
        val value: Any?,
        val instanceFlowHeaders: InstanceFlowHeaders,
        val headers: Map<String, Any?>,
    ) {
        fun header(key: String): String? =
            when (val value = headers[key]) {
                null -> null
                is ByteArray -> String(value, UTF_8)
                else -> value.toString()
            }
    }

    /** One listener per class the flyt apps read with, each in its own group, on the topics of the cases using it. */
    class FixtureListeners {
        private val consumed = ConcurrentHashMap<Pair<String, Class<*>>, CompletableFuture<Consumed>>()

        fun future(
            marker: String,
            type: Class<*>,
        ): CompletableFuture<Consumed> = consumed.computeIfAbsent(marker to type) { CompletableFuture() }

        fun topicsFor(type: String): Array<String> =
            WireCompatCases.all
                .filter { case -> case.consumers.any { it.type.simpleName == type } }
                .map { it.topic }
                .distinct()
                .toTypedArray()

        @KafkaListener(
            topics = ["#{__listener.topicsFor('InstanceObjectDto')}"],
            groupId = "#{@kafkaGroupIds.withSuffix('-wire-compat-InstanceObjectDto')}",
        )
        fun instanceObjectDto(
            @Payload(required = false) value: InstanceObjectDto?,
            @Header(INSTANCE_FLOW_HEADERS_KEY) instanceFlowHeaders: InstanceFlowHeaders,
            @Headers headers: Map<String, Any?>,
        ) = record(InstanceObjectDto::class.java, value, instanceFlowHeaders, headers)

        @KafkaListener(
            topics = ["#{__listener.topicsFor('MappingInstanceObject')}"],
            groupId = "#{@kafkaGroupIds.withSuffix('-wire-compat-MappingInstanceObject')}",
        )
        fun mappingInstanceObject(
            value: MappingInstanceObject,
            @Header(INSTANCE_FLOW_HEADERS_KEY) instanceFlowHeaders: InstanceFlowHeaders,
            @Headers headers: Map<String, Any?>,
        ) = record(MappingInstanceObject::class.java, value, instanceFlowHeaders, headers)

        @KafkaListener(
            topics = ["#{__listener.topicsFor('ArchiveInstance')}"],
            groupId = "#{@kafkaGroupIds.withSuffix('-wire-compat-ArchiveInstance')}",
        )
        fun archiveInstance(
            value: ArchiveInstance,
            @Header(INSTANCE_FLOW_HEADERS_KEY) instanceFlowHeaders: InstanceFlowHeaders,
            @Headers headers: Map<String, Any?>,
        ) = record(ArchiveInstance::class.java, value, instanceFlowHeaders, headers)

        @KafkaListener(
            topics = ["#{__listener.topicsFor('InstanceErrorEvent')}"],
            groupId = "#{@kafkaGroupIds.withSuffix('-wire-compat-InstanceErrorEvent')}",
        )
        fun instanceErrorEvent(
            value: InstanceErrorEvent,
            @Header(INSTANCE_FLOW_HEADERS_KEY) instanceFlowHeaders: InstanceFlowHeaders,
            @Headers headers: Map<String, Any?>,
        ) = record(InstanceErrorEvent::class.java, value, instanceFlowHeaders, headers)

        @KafkaListener(
            topics = ["#{__listener.topicsFor('ErrorCollection')}"],
            groupId = "#{@kafkaGroupIds.withSuffix('-wire-compat-ErrorCollection')}",
        )
        fun errorCollection(
            value: ErrorCollection,
            @Header(INSTANCE_FLOW_HEADERS_KEY) instanceFlowHeaders: InstanceFlowHeaders,
            @Headers headers: Map<String, Any?>,
        ) = record(ErrorCollection::class.java, value, instanceFlowHeaders, headers)

        @KafkaListener(
            topics = ["#{__listener.topicsFor('Object')}"],
            groupId = "#{@kafkaGroupIds.withSuffix('-wire-compat-Object')}",
        )
        fun any(
            @Payload(required = false) value: Any?,
            @Header(INSTANCE_FLOW_HEADERS_KEY) instanceFlowHeaders: InstanceFlowHeaders,
            @Headers headers: Map<String, Any?>,
        ) = record(Any::class.java, value, instanceFlowHeaders, headers)

        private fun record(
            type: Class<*>,
            value: Any?,
            instanceFlowHeaders: InstanceFlowHeaders,
            headers: Map<String, Any?>,
        ) {
            val consumed = Consumed(value, instanceFlowHeaders, headers)
            future(requireNotNull(consumed.header(MARKER_HEADER)), type).complete(consumed)
        }
    }

    @TestConfiguration
    class ListenerConfiguration {
        @Bean
        fun fixtureListeners() = FixtureListeners()

        /** Created before the listeners start, so they are assigned partitions at once. */
        @Bean
        fun fixtureTopics() =
            KafkaAdmin.NewTopics(
                *WireCompatCases.all
                    .map { it.topic }
                    .distinct()
                    .map { TopicBuilder.name(it).build() }
                    .toTypedArray(),
            )
    }

    private companion object {
        /** Added by the test so it can tell the records apart. */
        const val MARKER_HEADER = "wire-compat.fixture"

        val fixtureMapper = jacksonObjectMapper()
    }
}
