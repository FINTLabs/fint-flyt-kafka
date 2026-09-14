package no.novari.flyt.kafka.instanceflow.producing

import no.novari.flyt.kafka.instanceflow.headers.INSTANCE_FLOW_HEADERS_KEY
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.kafka.topic.name.TopicNameParameters
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders

data class InstanceFlowProducerRecord<V>(
    val topicNameParameters: TopicNameParameters,
    val instanceFlowHeaders: InstanceFlowHeaders,
    val key: String?,
    val value: V?,
    val additionalHeaders: Headers = RecordHeaders().apply { setReadOnly() },
) {
    // Matches the Java v6 behaviour (no @ToString) so that the producer record payload is
    // not exposed through default logging of the data class.
    override fun toString(): String = "${javaClass.name}@${Integer.toHexString(hashCode())}"

    class Builder<V> internal constructor() {
        private var topicNameParameters: TopicNameParameters? = null
        private var instanceFlowHeaders: InstanceFlowHeaders? = null
        private var key: String? = null
        private var value: V? = null
        private val additionalHeaders = RecordHeaders()

        fun topicNameParameters(params: TopicNameParameters?) = apply { this.topicNameParameters = params }

        fun instanceFlowHeaders(headers: InstanceFlowHeaders?) = apply { this.instanceFlowHeaders = headers }

        fun additionalHeader(header: Header?) =
            apply {
                header?.let {
                    validateAdditionalHeaderKey(it.key())
                    additionalHeaders.add(it)
                }
            }

        fun additionalHeader(
            key: String,
            value: ByteArray?,
        ) = apply {
            validateAdditionalHeaderKey(key)
            value?.let { additionalHeaders.add(RecordHeader(key, it)) }
        }

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
                additionalHeaders = RecordHeaders(additionalHeaders).apply { setReadOnly() },
            )
    }

    companion object {
        @JvmStatic
        fun <V> builder(): Builder<V> = Builder()

        private fun validateAdditionalHeaderKey(key: String) {
            require(key != INSTANCE_FLOW_HEADERS_KEY) {
                "Header key '$INSTANCE_FLOW_HEADERS_KEY' is reserved for InstanceFlowHeaders"
            }
        }
    }
}
