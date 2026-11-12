package no.novari.flyt.kafka.wirecompat

import no.novari.flyt.kafka.instanceflow.producing.send
import no.novari.kafka.starter.topic.KafkaTopics
import no.novari.kafka.starter.topic.TopicName
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestConstructor
import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Produces every [WireCompatCases] case with this version of fint-flyt-kafka, and compares the record on Kafka with the
 * checked-in fixture in `wire-compat/fixtures/<VERSION>`. `compat-v7` checks that fint-flyt-kafka 7 can read the
 * fixtures.
 *
 * Write the fixtures with `./gradlew test -PwriteWireCompatFixtures`. Fixtures of an older version must not be
 * rewritten: they are what that version put on Kafka.
 */
@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ProduceFixturesTest(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val topics: KafkaTopics,
    private val embeddedKafka: EmbeddedKafkaBroker,
) {
    @TestFactory
    fun `records on Kafka match the fixtures`(): List<DynamicTest> =
        WireCompatCases.all.map { case ->
            dynamicTest(case.name) {
                val actual = produce(case)
                val file = WireCompatFixtures.file(VERSION, case.name)
                if (System.getProperty("wireCompat.write").toBoolean()) {
                    file.parentFile.mkdirs()
                    file.writeText(fixtureWriter.writeValueAsString(actual) + "\n")
                } else {
                    assertTrue(file.exists()) { "$file is missing. Write it with -PwriteWireCompatFixtures." }
                    assertSemanticallyEqual(fixtureMapper.readValue(file), actual)
                }
            }
        }

    private fun produce(case: WireCompatCase): FixtureRecord {
        val metadata =
            kafkaTemplate
                .send(
                    topicName(case),
                    case.instanceFlowHeaders,
                    case.value,
                    additionalHeaders = case.additionalHeaders.mapValues { it.value.toByteArray(UTF_8) },
                ).get(10, TimeUnit.SECONDS)
                .recordMetadata
        assertEquals(case.topic, metadata.topic())
        return read(case, metadata)
    }

    private fun read(
        case: WireCompatCase,
        metadata: RecordMetadata,
    ): FixtureRecord {
        val partition = TopicPartition(metadata.topic(), metadata.partition())
        val properties =
            mapOf<String, Any>(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to embeddedKafka.brokersAsString,
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            )
        KafkaConsumer(properties, ByteArrayDeserializer(), ByteArrayDeserializer()).use { consumer ->
            consumer.assign(listOf(partition))
            consumer.seek(partition, metadata.offset())
            val deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos()
            while (System.nanoTime() < deadline) {
                val record =
                    consumer
                        .poll(Duration.ofMillis(500))
                        .records(partition)
                        .firstOrNull { it.offset() == metadata.offset() }
                if (record != null) {
                    return FixtureRecord(
                        case = case.name,
                        topic = record.topic(),
                        key = record.key()?.let(::utf8),
                        headers = record.headers().map { FixtureHeader(it.key(), utf8(it.value())) },
                        value = record.value()?.let(::utf8),
                    )
                }
            }
        }
        throw AssertionError("${case.name}: record at $partition@${metadata.offset()} was not read")
    }

    private fun topicName(case: WireCompatCase): TopicName =
        when (case.topicType) {
            TopicType.EVENT -> {
                topics.event(case.eventName, WireCompatCases.ORG_ID, WireCompatCases.DOMAIN_CONTEXT)
            }

            TopicType.ERROR_EVENT -> {
                topics.errorEvent(
                    case.eventName,
                    WireCompatCases.ORG_ID,
                    WireCompatCases.DOMAIN_CONTEXT,
                )
            }
        }

    /**
     * The fixture's topic, key and headers must be on the record. JSON is compared as a tree, so field order does not
     * matter. Headers the fixture does not have are allowed.
     */
    private fun assertSemanticallyEqual(
        expected: FixtureRecord,
        actual: FixtureRecord,
    ) {
        assertEquals(expected.topic, actual.topic, "topic")
        assertEquals(expected.key, actual.key, "key")
        expected.headers.forEach { header ->
            val actualValue = actual.headers.lastOrNull { it.key == header.key }?.value
            assertNotNull(actualValue) { "header ${header.key} is missing" }
            if (header.key in JSON_HEADERS) {
                assertEquals(json(header.value), json(actualValue!!), "header ${header.key}")
            } else {
                assertEquals(header.value, actualValue, "header ${header.key}")
            }
        }
        assertEquals(expected.value?.let(::json), actual.value?.let(::json), "value")
    }

    private fun json(value: String): JsonNode = fixtureMapper.readTree(value)

    private fun utf8(bytes: ByteArray): String =
        UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()

    private companion object {
        /** The fixtures this version writes. */
        const val VERSION = "v8"

        val JSON_HEADERS = setOf("flyt.instance-flow-headers", WireCompatCases.ACTOR_HEADER)

        val fixtureMapper = jacksonObjectMapper()
        val fixtureWriter = fixtureMapper.writerWithDefaultPrettyPrinter()
    }
}
