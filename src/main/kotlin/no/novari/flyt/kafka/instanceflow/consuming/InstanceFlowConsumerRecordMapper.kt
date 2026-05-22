package no.novari.flyt.kafka.instanceflow.consuming

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Service

@Service
class InstanceFlowConsumerRecordMapper(
    private val instanceFlowHeadersMapper: InstanceFlowHeadersMapper,
) {
    fun <T> toInstanceFlowConsumerRecord(consumerRecord: ConsumerRecord<String, T>): InstanceFlowConsumerRecord<T> =
        InstanceFlowConsumerRecord(
            instanceFlowHeaders = instanceFlowHeadersMapper.getInstanceFlowHeaders(consumerRecord.headers()),
            consumerRecord = consumerRecord,
        )

    fun <T> toInstanceFlowConsumerRecords(
        consumerRecords: List<ConsumerRecord<String, T>>,
    ): List<InstanceFlowConsumerRecord<T>> = consumerRecords.map(::toInstanceFlowConsumerRecord)
}
