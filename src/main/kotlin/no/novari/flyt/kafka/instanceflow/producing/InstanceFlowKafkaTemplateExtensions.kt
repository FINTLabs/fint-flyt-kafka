package no.novari.flyt.kafka.instanceflow.producing

import no.novari.flyt.kafka.instanceflow.headers.INSTANCE_FLOW_HEADERS_KEY
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper
import no.novari.kafka.starter.topic.TopicName
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

/**
 * Sends [value] to [topic] with [instanceFlowHeaders] in the header `flyt.instance-flow-headers`. A `null` value is a
 * tombstone. [additionalHeaders] are added as they are, and must not use the instance flow header key.
 */
public fun <V : Any> KafkaTemplate<String, V>.send(
    topic: TopicName,
    instanceFlowHeaders: InstanceFlowHeaders,
    value: V?,
    key: String? = null,
    additionalHeaders: Map<String, ByteArray> = emptyMap(),
): CompletableFuture<SendResult<String, V>> {
    require(INSTANCE_FLOW_HEADERS_KEY !in additionalHeaders) {
        "Header key '$INSTANCE_FLOW_HEADERS_KEY' is reserved for InstanceFlowHeaders"
    }
    val headers =
        additionalHeaders.map { (headerKey, headerValue) -> RecordHeader(headerKey, headerValue) } +
            InstanceFlowHeadersMapper.toHeader(instanceFlowHeaders)
    return send(ProducerRecord(topic.name, null, key, value, headers))
}
