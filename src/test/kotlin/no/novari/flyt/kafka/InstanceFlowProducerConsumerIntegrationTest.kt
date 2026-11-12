package no.novari.flyt.kafka

import no.novari.flyt.kafka.instanceflow.headers.INSTANCE_FLOW_HEADERS_KEY
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.send
import no.novari.flyt.kafka.model.Error
import no.novari.flyt.kafka.model.ErrorCollection
import no.novari.kafka.starter.topic.KafkaTopics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestConstructor
import java.nio.charset.StandardCharsets.UTF_8
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class InstanceFlowProducerConsumerIntegrationTest(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val topics: KafkaTopics,
    private val listeners: Listeners,
) {
    data class TestObject(
        val integer: Int?,
        val string: String?,
    )

    data class Consumed<T>(
        val value: T?,
        val instanceFlowHeaders: InstanceFlowHeaders,
        val actor: String? = null,
        val contract: String? = null,
    )

    class Listeners {
        val event = CompletableFuture<Consumed<TestObject>>()
        val eventWithAdditionalHeaders = CompletableFuture<Consumed<TestObject>>()
        val errorEvent = CompletableFuture<Consumed<ErrorCollection>>()
        val entity = CompletableFuture<Consumed<String>>()
        val tombstone = CompletableFuture<InstanceFlowHeaders>()

        /** As instance-dispatched and instance-deleted: no value, only the headers. */
        @KafkaListener(topics = ["test-org-id.test-domain-context.event.tombstone"])
        fun tombstone(
            @Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders,
        ) {
            tombstone.complete(headers)
        }

        @KafkaListener(topics = ["test-org-id.test-domain-context.event.event"])
        fun event(
            value: TestObject,
            @Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders,
        ) {
            event.complete(Consumed(value, headers))
        }

        @KafkaListener(topics = ["test-org-id.test-domain-context.event.event-with-additional-headers"])
        fun eventWithAdditionalHeaders(
            value: TestObject,
            @Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders,
            @Header("flyt.actor") actor: ByteArray,
            @Header("adapter-health.contract") contract: ByteArray,
        ) {
            eventWithAdditionalHeaders.complete(Consumed(value, headers, String(actor, UTF_8), String(contract, UTF_8)))
        }

        @KafkaListener(topics = ["test-org-id.test-domain-context.event.error.event"])
        fun errorEvent(
            value: ErrorCollection,
            @Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders,
        ) {
            errorEvent.complete(Consumed(value, headers))
        }

        @KafkaListener(topics = ["test-org-id.test-domain-context.entity.resource"])
        fun entity(
            @Payload(required = false) value: String?,
            @Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders,
        ) {
            entity.complete(Consumed(value, headers))
        }
    }

    @TestConfiguration
    class ListenerConfiguration {
        @Bean
        fun listeners() = Listeners()
    }

    @Test
    fun `event roundtrips through Kafka with instance flow headers and value preserved`() {
        val testObject = TestObject(2, "testObjectString")

        kafkaTemplate.send(topics.event("event"), createInstanceFlowHeaders(), testObject)

        assertEquals(Consumed(testObject, createInstanceFlowHeaders()), listeners.event.get(10, TimeUnit.SECONDS))
    }

    @Test
    fun `event roundtrips through Kafka with additional headers preserved`() {
        val testObject = TestObject(3, "testObjectWithHeader")
        val actorHeaderValue = """{"type":"USER","oid":"2ee6f95e-44c3-11ed-b878-0242ac120002"}"""
        val contractHeaderValue = "adapter-health-v1"

        kafkaTemplate.send(
            topics.event("event-with-additional-headers"),
            createInstanceFlowHeaders(),
            testObject,
            additionalHeaders =
                mapOf(
                    "flyt.actor" to actorHeaderValue.toByteArray(UTF_8),
                    "adapter-health.contract" to contractHeaderValue.toByteArray(UTF_8),
                ),
        )

        assertEquals(
            Consumed(testObject, createInstanceFlowHeaders(), actorHeaderValue, contractHeaderValue),
            listeners.eventWithAdditionalHeaders.get(10, TimeUnit.SECONDS),
        )
    }

    @Test
    fun `error event roundtrips through Kafka with ErrorCollection payload preserved`() {
        val errorCollection =
            ErrorCollection(
                (1..3).map {
                    Error(
                        errorCode = "ERROR_CODE_$it",
                        args =
                            mapOf(
                                "arg1" to "argValue1",
                                "arg2" to "argValue2",
                            ),
                    )
                },
            )

        kafkaTemplate.send(topics.errorEvent("event"), createInstanceFlowHeaders(), errorCollection)

        assertEquals(
            Consumed(errorCollection, createInstanceFlowHeaders()),
            listeners.errorEvent.get(10, TimeUnit.SECONDS),
        )
    }

    @Test
    fun `entity roundtrips through Kafka with String payload preserved`() {
        kafkaTemplate.send(topics.entity("resource"), createInstanceFlowHeaders(), "valueString", key = "key")

        assertEquals(Consumed("valueString", createInstanceFlowHeaders()), listeners.entity.get(10, TimeUnit.SECONDS))
    }

    @Test
    fun `tombstone is read by a listener without a value parameter`() {
        kafkaTemplate.send(topics.event("tombstone"), createInstanceFlowHeaders(), null)

        assertEquals(createInstanceFlowHeaders(), listeners.tombstone.get(10, TimeUnit.SECONDS))
    }

    @Test
    fun `additional headers reject the reserved instance flow headers key`() {
        assertThrows(IllegalArgumentException::class.java) {
            kafkaTemplate.send(
                topics.event("event"),
                createInstanceFlowHeaders(),
                "value",
                additionalHeaders = mapOf(INSTANCE_FLOW_HEADERS_KEY to "override".toByteArray(UTF_8)),
            )
        }
    }

    private fun createInstanceFlowHeaders() =
        InstanceFlowHeaders(
            sourceApplicationId = 1,
            correlationId = UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"),
            sourceApplicationIntegrationId = "sourceApplicationIntegrationId",
            sourceApplicationInstanceId = "sourceApplicationInstanceId",
        )
}
