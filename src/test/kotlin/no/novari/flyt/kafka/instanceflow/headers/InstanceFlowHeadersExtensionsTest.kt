package no.novari.flyt.kafka.instanceflow.headers

import org.apache.kafka.common.header.internals.RecordHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class InstanceFlowHeadersExtensionsTest {
    private val instanceFlowHeaders =
        InstanceFlowHeaders(sourceApplicationId = 1, correlationId = UUID.randomUUID(), instanceId = 2)

    @Test
    fun `reads the header it writes`() {
        val headers = RecordHeaders().add(InstanceFlowHeadersMapper.write(instanceFlowHeaders))

        assertEquals(instanceFlowHeaders, headers.instanceFlowHeaders())
        assertEquals(instanceFlowHeaders, headers.instanceFlowHeadersOrNull())
    }

    @Test
    fun `missing header`() {
        assertThrows<NoInstanceFlowHeadersException> { RecordHeaders().instanceFlowHeaders() }
        assertNull(RecordHeaders().instanceFlowHeadersOrNull())
    }

    @Test
    fun `header that cannot be read`() {
        val headers = RecordHeaders().add(INSTANCE_FLOW_HEADERS_KEY, """{"sourceApplicationId":"x"}""".toByteArray())

        assertThrows<CouldNotReadInstanceFlowHeadersException> { headers.instanceFlowHeaders() }
        assertNull(headers.instanceFlowHeadersOrNull())
    }
}
