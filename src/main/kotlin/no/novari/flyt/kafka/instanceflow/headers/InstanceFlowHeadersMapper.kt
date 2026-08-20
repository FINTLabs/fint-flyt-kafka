package no.novari.flyt.kafka.instanceflow.headers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.observation.ObservationRegistry
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class InstanceFlowHeadersMapper(
    private val objectMapper: ObjectMapper,
    private val observationRegistry: ObjectProvider<ObservationRegistry>,
) {
    fun toHeader(instanceFlowHeaders: InstanceFlowHeaders?): Header {
        if (instanceFlowHeaders == null) {
            throw NoInstanceFlowHeadersException()
        }
        tagCurrentSpanWithCorrelationId(instanceFlowHeaders)
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
            objectMapper.readValue(header.value(), InstanceFlowHeaders::class.java).also {
                tagCurrentSpanWithCorrelationId(it)
            }
        } catch (_: IOException) {
            throw CouldNotReadInstanceFlowHeadersException(header)
        }

    private fun tagCurrentSpanWithCorrelationId(instanceFlowHeaders: InstanceFlowHeaders) {
        observationRegistry.ifAvailable { registry ->
            registry.currentObservation?.highCardinalityKeyValue(
                CORRELATION_ID_ATTRIBUTE_KEY,
                instanceFlowHeaders.correlationId.toString(),
            )
        }
    }

    companion object {
        private const val INSTANCE_FLOW_HEADERS_KEY = "flyt.instance-flow-headers"
        private const val CORRELATION_ID_ATTRIBUTE_KEY = "flyt.correlation-id"
    }
}
