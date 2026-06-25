package no.novari.flyt.kafka.instanceflow.consuming

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.apache.kafka.clients.consumer.ConsumerRecord

data class InstanceFlowConsumerRecord<T>(
    val instanceFlowHeaders: InstanceFlowHeaders,
    val consumerRecord: ConsumerRecord<String, T>,
) {
    // Matches the Java v6 behaviour (no @ToString) so that the consumer record payload is
    // not exposed through default logging of the data class.
    override fun toString(): String = "${javaClass.name}@${Integer.toHexString(hashCode())}"

    class Builder<T> internal constructor() {
        private var instanceFlowHeaders: InstanceFlowHeaders? = null
        private var consumerRecord: ConsumerRecord<String, T>? = null

        fun instanceFlowHeaders(headers: InstanceFlowHeaders?) = apply { this.instanceFlowHeaders = headers }

        fun consumerRecord(record: ConsumerRecord<String, T>?) = apply { this.consumerRecord = record }

        fun build(): InstanceFlowConsumerRecord<T> =
            InstanceFlowConsumerRecord(
                instanceFlowHeaders =
                    instanceFlowHeaders
                        ?: throw NullPointerException("instanceFlowHeaders is marked non-null but is null"),
                consumerRecord =
                    consumerRecord
                        ?: throw NullPointerException("consumerRecord is marked non-null but is null"),
            )
    }

    companion object {
        @JvmStatic
        fun <T> builder(): Builder<T> = Builder()
    }
}
