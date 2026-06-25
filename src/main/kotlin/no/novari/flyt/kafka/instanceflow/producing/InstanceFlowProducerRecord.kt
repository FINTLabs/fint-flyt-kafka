package no.novari.flyt.kafka.instanceflow.producing

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.kafka.topic.name.TopicNameParameters

data class InstanceFlowProducerRecord<V>(
    val topicNameParameters: TopicNameParameters,
    val instanceFlowHeaders: InstanceFlowHeaders,
    val key: String?,
    val value: V?,
) {
    // Matches the Java v6 behaviour (no @ToString) so that the producer record payload is
    // not exposed through default logging of the data class.
    override fun toString(): String = "${javaClass.name}@${Integer.toHexString(hashCode())}"

    class Builder<V> internal constructor() {
        private var topicNameParameters: TopicNameParameters? = null
        private var instanceFlowHeaders: InstanceFlowHeaders? = null
        private var key: String? = null
        private var value: V? = null

        fun topicNameParameters(params: TopicNameParameters?) = apply { this.topicNameParameters = params }

        fun instanceFlowHeaders(headers: InstanceFlowHeaders?) = apply { this.instanceFlowHeaders = headers }

        fun key(key: String?) = apply { this.key = key }

        fun value(value: V?) = apply { this.value = value }

        fun build(): InstanceFlowProducerRecord<V> =
            InstanceFlowProducerRecord(
                topicNameParameters =
                    topicNameParameters
                        ?: throw NullPointerException("topicNameParameters is marked non-null but is null"),
                instanceFlowHeaders =
                    instanceFlowHeaders
                        ?: throw NullPointerException("instanceFlowHeaders is marked non-null but is null"),
                key = key,
                value = value,
            )
    }

    companion object {
        @JvmStatic
        fun <V> builder(): Builder<V> = Builder()
    }
}
