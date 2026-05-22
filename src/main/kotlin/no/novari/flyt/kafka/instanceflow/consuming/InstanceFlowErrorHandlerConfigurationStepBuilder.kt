package no.novari.flyt.kafka.instanceflow.consuming

import no.novari.kafka.consuming.ErrorHandlerConfiguration
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.util.backoff.BackOff
import org.springframework.util.backoff.ExponentialBackOff
import org.springframework.util.backoff.FixedBackOff
import java.time.Duration
import java.util.function.BiFunction

class InstanceFlowErrorHandlerConfigurationStepBuilder private constructor() {
    interface RetryStep<VALUE> :
        DefaultRetryStep<VALUE>,
        RetryFunctionStep<VALUE>

    interface RetryFunctionStep<VALUE> {
        fun retryWithBackoffFunction(
            backoffFunction: BiFunction<InstanceFlowConsumerRecord<VALUE>, Exception, BackOff?>,
        ): RetryFunctionDefaultStep<VALUE>
    }

    interface RetryFunctionDefaultStep<VALUE> {
        fun orElse(): DefaultRetryStep<VALUE>
    }

    interface DefaultRetryStep<VALUE> {
        fun noRetries(): RecoveryStep<VALUE>

        fun retryWithFixedInterval(
            interval: Duration,
            maxRetries: Int,
        ): RetryClassificationStep<VALUE>

        fun retryWithExponentialInterval(
            initialInterval: Duration,
            intervalMultiplier: Double,
            maxInterval: Duration,
            maxElapsedTime: Duration,
        ): RetryClassificationStep<VALUE>

        fun retryWithExponentialInterval(
            initialInterval: Duration,
            intervalMultiplier: Double,
            maxInterval: Duration,
            maxRetries: Int,
        ): RetryClassificationStep<VALUE>
    }

    interface RetryClassificationStep<VALUE> {
        fun retryOnly(exceptions: Collection<Class<out Exception>>): RetryFailureChangeStep<VALUE>

        fun excludeExceptionsFromRetry(exceptions: Collection<Class<out Exception>>): RetryFailureChangeStep<VALUE>

        fun useDefaultRetryClassification(): RetryFailureChangeStep<VALUE>
    }

    interface RetryFailureChangeStep<VALUE> {
        fun restartRetryOnExceptionChange(): RecoveryStep<VALUE>

        fun continueRetryOnExceptionChange(): RecoveryStep<VALUE>
    }

    interface RecoveryStep<VALUE> {
        fun skipFailedRecords(): BuilderStep<VALUE>

        fun recoverFailedRecords(
            customRecoverer: InstanceFlowErrorHandlerConfiguration.InstanceFlowRecoverer<VALUE>,
        ): RecoveryFailureStep<VALUE>
    }

    interface RecoveryFailureStep<VALUE> {
        fun skipRecordOnRecoveryFailure(): BuilderStep<VALUE>

        fun reprocessAndRetryRecordOnRecoveryFailure(): BuilderStep<VALUE>

        fun reprocessRecordOnRecoveryFailure(): BuilderStep<VALUE>
    }

    interface BuilderStep<VALUE> {
        fun build(): InstanceFlowErrorHandlerConfiguration<VALUE>
    }

    private class Steps<VALUE> :
        RetryStep<VALUE>,
        DefaultRetryStep<VALUE>,
        RetryFunctionDefaultStep<VALUE>,
        RetryClassificationStep<VALUE>,
        RetryFailureChangeStep<VALUE>,
        RecoveryStep<VALUE>,
        RecoveryFailureStep<VALUE>,
        BuilderStep<VALUE> {
        private var defaultBackOff: BackOff? = null
        private var backOffFunction: BiFunction<InstanceFlowConsumerRecord<VALUE>, Exception, BackOff?>? = null
        private var restartRetryOnExceptionChange: Boolean = false
        private var customRecoverer: InstanceFlowErrorHandlerConfiguration.InstanceFlowRecoverer<VALUE>? = null
        private var skipRecordOnRecoveryFailure: Boolean = false
        private var restartRetryOnRecoveryFailure: Boolean = false
        private var classificationType: ErrorHandlerConfiguration.ClassificationType? = null
        private var classificationExceptions: Collection<Class<out Exception>>? = null

        override fun noRetries(): RecoveryStep<VALUE> = this

        override fun retryWithBackoffFunction(
            backoffFunction: BiFunction<InstanceFlowConsumerRecord<VALUE>, Exception, BackOff?>,
        ): RetryFunctionDefaultStep<VALUE> {
            backOffFunction = backoffFunction
            return this
        }

        override fun orElse(): DefaultRetryStep<VALUE> = this

        override fun retryWithFixedInterval(
            interval: Duration,
            maxRetries: Int,
        ): RetryClassificationStep<VALUE> {
            defaultBackOff = FixedBackOff(interval.toMillis(), maxRetries.toLong())
            return this
        }

        override fun retryWithExponentialInterval(
            initialInterval: Duration,
            intervalMultiplier: Double,
            maxInterval: Duration,
            maxElapsedTime: Duration,
        ): RetryClassificationStep<VALUE> {
            val exponentialBackOff = ExponentialBackOff(initialInterval.toMillis(), intervalMultiplier)
            exponentialBackOff.maxInterval = maxInterval.toMillis()
            exponentialBackOff.maxElapsedTime = maxElapsedTime.toMillis()
            defaultBackOff = exponentialBackOff
            return this
        }

        override fun retryWithExponentialInterval(
            initialInterval: Duration,
            intervalMultiplier: Double,
            maxInterval: Duration,
            maxRetries: Int,
        ): RetryClassificationStep<VALUE> {
            val exponentialBackOff = ExponentialBackOffWithMaxRetries(maxRetries)
            exponentialBackOff.initialInterval = initialInterval.toMillis()
            exponentialBackOff.multiplier = intervalMultiplier
            exponentialBackOff.maxInterval = maxInterval.toMillis()
            defaultBackOff = exponentialBackOff
            return this
        }

        override fun retryOnly(exceptions: Collection<Class<out Exception>>): RetryFailureChangeStep<VALUE> {
            classificationType = ErrorHandlerConfiguration.ClassificationType.ONLY
            classificationExceptions = exceptions
            return this
        }

        override fun excludeExceptionsFromRetry(
            exceptions: Collection<Class<out Exception>>,
        ): RetryFailureChangeStep<VALUE> {
            classificationType = ErrorHandlerConfiguration.ClassificationType.EXCLUDE
            classificationExceptions = exceptions
            return this
        }

        override fun useDefaultRetryClassification(): RetryFailureChangeStep<VALUE> {
            classificationType = ErrorHandlerConfiguration.ClassificationType.DEFAULT
            return this
        }

        override fun restartRetryOnExceptionChange(): RecoveryStep<VALUE> {
            restartRetryOnExceptionChange = true
            return this
        }

        override fun continueRetryOnExceptionChange(): RecoveryStep<VALUE> {
            restartRetryOnExceptionChange = false
            return this
        }

        override fun skipFailedRecords(): BuilderStep<VALUE> = this

        override fun recoverFailedRecords(
            customRecoverer: InstanceFlowErrorHandlerConfiguration.InstanceFlowRecoverer<VALUE>,
        ): RecoveryFailureStep<VALUE> {
            this.customRecoverer = customRecoverer
            return this
        }

        override fun skipRecordOnRecoveryFailure(): BuilderStep<VALUE> {
            skipRecordOnRecoveryFailure = true
            return this
        }

        override fun reprocessAndRetryRecordOnRecoveryFailure(): BuilderStep<VALUE> {
            restartRetryOnRecoveryFailure = true
            return this
        }

        override fun reprocessRecordOnRecoveryFailure(): BuilderStep<VALUE> {
            restartRetryOnRecoveryFailure = false
            return this
        }

        override fun build(): InstanceFlowErrorHandlerConfiguration<VALUE> =
            InstanceFlowErrorHandlerConfiguration(
                backOffFunction = backOffFunction,
                defaultBackoff = defaultBackOff,
                restartRetryOnExceptionChange = restartRetryOnExceptionChange,
                customRecoverer = customRecoverer,
                skipRecordOnRecoveryFailure = skipRecordOnRecoveryFailure,
                restartRetryOnRecoveryFailure = restartRetryOnRecoveryFailure,
                classificationType = classificationType,
                classificationExceptions = classificationExceptions,
            )
    }

    companion object {
        @JvmStatic
        fun <VALUE> firstStep(): RetryStep<VALUE> = Steps()
    }
}
