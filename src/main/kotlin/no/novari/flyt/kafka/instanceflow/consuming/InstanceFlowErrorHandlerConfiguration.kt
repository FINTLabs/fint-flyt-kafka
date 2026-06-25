package no.novari.flyt.kafka.instanceflow.consuming

import no.novari.kafka.consuming.ErrorHandlerConfiguration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.util.backoff.BackOff
import java.util.function.BiFunction

data class InstanceFlowErrorHandlerConfiguration<VALUE>(
    val backOffFunction: BiFunction<InstanceFlowConsumerRecord<VALUE>, Exception, BackOff?>? = null,
    val defaultBackoff: BackOff? = null,
    val customRecoverer: InstanceFlowRecoverer<VALUE>? = null,
    val classificationType: ErrorHandlerConfiguration.ClassificationType? = null,
    val classificationExceptions: Collection<Class<out Exception>>? = null,
    val skipRecordOnRecoveryFailure: Boolean = false,
    val restartRetryOnExceptionChange: Boolean = true,
    val restartRetryOnRecoveryFailure: Boolean = true,
) {
    interface InstanceFlowRecoverer<VALUE> {
        fun recover(
            instanceFlowConsumerRecord: InstanceFlowConsumerRecord<VALUE>,
            exception: Exception,
        )

        fun recoverForMissingInstanceFlowHeaders(
            consumerRecord: ConsumerRecord<String, VALUE>,
            exception: Exception,
        )
    }

    companion object {
        @JvmStatic
        fun <VALUE> stepBuilder(): InstanceFlowErrorHandlerConfigurationStepBuilder.RetryStep<VALUE> =
            InstanceFlowErrorHandlerConfigurationStepBuilder.firstStep()
    }
}
