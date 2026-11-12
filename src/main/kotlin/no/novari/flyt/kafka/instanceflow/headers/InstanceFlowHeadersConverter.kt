package no.novari.flyt.kafka.instanceflow.headers

import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.core.convert.converter.Converter

/**
 * Lets a `@KafkaListener` take the header as a parameter: `@Header(INSTANCE_FLOW_HEADERS_KEY) headers:
 * InstanceFlowHeaders`. Spring Kafka uses `Converter` beans when it resolves listener arguments.
 */
internal class InstanceFlowHeadersConverter : Converter<ByteArray, InstanceFlowHeaders> {
    override fun convert(source: ByteArray): InstanceFlowHeaders =
        InstanceFlowHeadersMapper.toInstanceFlowHeaders(RecordHeader(INSTANCE_FLOW_HEADERS_KEY, source))
}
