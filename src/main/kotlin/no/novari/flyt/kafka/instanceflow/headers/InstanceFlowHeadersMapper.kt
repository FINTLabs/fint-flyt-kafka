package no.novari.flyt.kafka.instanceflow.headers

import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

/** The Kafka header that holds [InstanceFlowHeaders] as JSON. */
public const val INSTANCE_FLOW_HEADERS_KEY: String = "flyt.instance-flow-headers"

/**
 * Writes and reads the header. Uses its own [JsonMapper], so the format does not depend on the application's Jackson
 * config: the header only holds ids, UUIDs and strings.
 */
public object InstanceFlowHeadersMapper {
    private val jsonMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

    public fun toHeader(instanceFlowHeaders: InstanceFlowHeaders?): Header {
        if (instanceFlowHeaders == null) {
            throw NoInstanceFlowHeadersException()
        }
        return try {
            RecordHeader(INSTANCE_FLOW_HEADERS_KEY, jsonMapper.writeValueAsBytes(instanceFlowHeaders))
        } catch (_: JacksonException) {
            throw CouldNotWriteInstanceFlowHeadersException(instanceFlowHeaders)
        }
    }

    public fun getInstanceFlowHeaders(headers: Headers): InstanceFlowHeaders {
        val header = headers.lastHeader(INSTANCE_FLOW_HEADERS_KEY) ?: throw NoInstanceFlowHeadersException()
        return toInstanceFlowHeaders(header)
    }

    public fun toInstanceFlowHeaders(header: Header): InstanceFlowHeaders =
        try {
            jsonMapper.readValue(header.value(), InstanceFlowHeaders::class.java)
        } catch (_: JacksonException) {
            throw CouldNotReadInstanceFlowHeadersException(header)
        }
}
