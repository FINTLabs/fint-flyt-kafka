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
 * Reads [InstanceFlowHeaders] from the record headers, for example in a `ConsumerRecordRecoverer`.
 *
 * @throws NoInstanceFlowHeadersException if the header is missing
 * @throws CouldNotReadInstanceFlowHeadersException if the header cannot be read
 */
public fun Headers.instanceFlowHeaders(): InstanceFlowHeaders {
    val header = lastHeader(INSTANCE_FLOW_HEADERS_KEY) ?: throw NoInstanceFlowHeadersException()
    return InstanceFlowHeadersMapper.read(header)
}

/** Reads [InstanceFlowHeaders] from the record headers, or `null` if the header is missing or cannot be read. */
public fun Headers.instanceFlowHeadersOrNull(): InstanceFlowHeaders? =
    try {
        instanceFlowHeaders()
    } catch (_: NoInstanceFlowHeadersException) {
        null
    } catch (_: CouldNotReadInstanceFlowHeadersException) {
        null
    }

/**
 * Writes and reads the header. Uses its own [JsonMapper], so the format does not depend on the application's Jackson
 * config: the header only holds ids, UUIDs and strings.
 */
internal object InstanceFlowHeadersMapper {
    private val jsonMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

    fun write(instanceFlowHeaders: InstanceFlowHeaders): Header =
        try {
            RecordHeader(INSTANCE_FLOW_HEADERS_KEY, jsonMapper.writeValueAsBytes(instanceFlowHeaders))
        } catch (_: JacksonException) {
            throw CouldNotWriteInstanceFlowHeadersException(instanceFlowHeaders)
        }

    fun read(header: Header): InstanceFlowHeaders =
        try {
            jsonMapper.readValue(header.value(), InstanceFlowHeaders::class.java)
        } catch (_: JacksonException) {
            throw CouldNotReadInstanceFlowHeadersException(header)
        }
}
