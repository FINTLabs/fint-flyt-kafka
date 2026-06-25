package no.novari.flyt.kafka.instanceflow.headers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class InstanceFlowHeadersMapper(
    private val objectMapper: ObjectMapper,
) {
    fun toHeader(instanceFlowHeaders: InstanceFlowHeaders?): Header {
        if (instanceFlowHeaders == null) {
            throw NoInstanceFlowHeadersException()
        }
        return try {
            RecordHeader(INSTANCE_FLOW_HEADERS_KEY, objectMapper.writeValueAsBytes(instanceFlowHeaders))
        } catch (_: JsonProcessingException) {
            throw CouldNotWriteInstanceFlowHeadersException(instanceFlowHeaders)
        }
    }

    fun toHeaders(instanceFlowHeaders: InstanceFlowHeaders?): Headers =
        RecordHeaders().add(toHeader(instanceFlowHeaders))

    fun getInstanceFlowHeaders(headers: Headers): InstanceFlowHeaders {
        val header = headers.lastHeader(INSTANCE_FLOW_HEADERS_KEY) ?: throw NoInstanceFlowHeadersException()
        return toInstanceFlowHeaders(header)
    }

    fun toInstanceFlowHeaders(header: Header): InstanceFlowHeaders =
        try {
            objectMapper.readValue(header.value(), InstanceFlowHeaders::class.java)
        } catch (_: IOException) {
            throw CouldNotReadInstanceFlowHeadersException(header)
        }

    companion object {
        private const val INSTANCE_FLOW_HEADERS_KEY = "flyt.instance-flow-headers"
    }
}
