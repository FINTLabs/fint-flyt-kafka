package no.novari.flyt.kafka.instanceflow.producing

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.util.UUID

class InstanceFlowProducerRecordTest {
    private data class TestObject(
        val integer: Int?,
        val string: String?,
    )

    private val topicNameParameters =
        EventTopicNameParameters
            .builder()
            .topicNamePrefixParameters(
                TopicNamePrefixParameters
                    .stepBuilder()
                    .orgId("test-org-id")
                    .domainContext("test-domain-context")
                    .build(),
            ).eventName("event")
            .build()

    private val instanceFlowHeaders =
        InstanceFlowHeaders
            .builder()
            .sourceApplicationId(1L)
            .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
            .build()

    @Test
    fun `data class equality and hash code include additional headers`() {
        val withHeader =
            recordBuilder()
                .additionalHeader("flyt.actor", bytes("actor-one"))
                .build()
        val withSameHeader =
            recordBuilder()
                .additionalHeader("flyt.actor", bytes("actor-one"))
                .build()
        val withoutHeader =
            recordBuilder()
                .build()
        val withOtherHeader =
            recordBuilder()
                .additionalHeader("flyt.actor", bytes("actor-two"))
                .build()

        assertEquals(withHeader, withSameHeader)
        assertEquals(withHeader.hashCode(), withSameHeader.hashCode())
        assertNotEquals(withHeader, withoutHeader)
        assertNotEquals(withHeader, withOtherHeader)
    }

    @Test
    fun `copy keeps additional headers when changing another field`() {
        val record =
            recordBuilder()
                .additionalHeader("flyt.actor", bytes("actor-one"))
                .value(TestObject(1, "before"))
                .build()

        val copied = record.copy(value = TestObject(2, "after"))

        assertEquals("actor-one", headerValue(copied.additionalHeaders, "flyt.actor"))
    }

    @Test
    fun `builder copies additional headers so records remain independent`() {
        val builder =
            recordBuilder()
                .additionalHeader("first-header", bytes("first"))

        val firstRecord = builder.build()
        val secondRecord =
            builder
                .additionalHeader("second-header", bytes("second"))
                .build()

        assertEquals("first", headerValue(firstRecord.additionalHeaders, "first-header"))
        assertNull(firstRecord.additionalHeaders.lastHeader("second-header"))
        assertEquals("second", headerValue(secondRecord.additionalHeaders, "second-header"))
    }

    @Test
    fun `built records expose read-only additional headers`() {
        val record =
            recordBuilder()
                .additionalHeader("flyt.actor", bytes("actor-one"))
                .build()

        assertThrows(IllegalStateException::class.java) {
            record.additionalHeaders.add("injected", bytes("value"))
        }
    }

    @Test
    fun `additional header byte array overload ignores null values`() {
        val record =
            recordBuilder()
                .additionalHeader("optional-header", null)
                .build()

        assertNull(record.additionalHeaders.lastHeader("optional-header"))
    }

    @Test
    fun `additional headers reject the reserved instance flow headers key`() {
        assertThrows(IllegalArgumentException::class.java) {
            recordBuilder().additionalHeader("flyt.instance-flow-headers", bytes("override"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            recordBuilder().additionalHeader(RecordHeader("flyt.instance-flow-headers", bytes("override")))
        }
    }

    private fun recordBuilder(): InstanceFlowProducerRecord.Builder<TestObject> =
        InstanceFlowProducerRecord
            .builder<TestObject>()
            .topicNameParameters(topicNameParameters)
            .instanceFlowHeaders(instanceFlowHeaders)

    private fun bytes(value: String): ByteArray = value.toByteArray(StandardCharsets.UTF_8)

    private fun headerValue(
        headers: Headers,
        key: String,
    ): String = String(headers.lastHeader(key).value(), StandardCharsets.UTF_8)
}
