package no.novari.flyt.kafka.instanceflow.consuming

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.apache.kafka.clients.consumer.ConsumerRecord

data class InstanceFlowConsumerRecord<T>(
    val instanceFlowHeaders: InstanceFlowHeaders?,
    val consumerRecord: ConsumerRecord<String, T>?,
) {
    class Builder<T> internal constructor() {
        private var instanceFlowHeaders: InstanceFlowHeaders? = null
        private var consumerRecord: ConsumerRecord<String, T>? = null

        fun instanceFlowHeaders(headers: InstanceFlowHeaders?) = apply { this.instanceFlowHeaders = headers }

        fun consumerRecord(record: ConsumerRecord<String, T>?) = apply { this.consumerRecord = record }

        fun build(): InstanceFlowConsumerRecord<T> =
            InstanceFlowConsumerRecord(
                instanceFlowHeaders = instanceFlowHeaders,
                consumerRecord = consumerRecord,
            )
    }

    companion object {
        @JvmStatic
        fun <T> builder(): Builder<T> = Builder()
    }
}
