package no.novari.flyt.kafka.instanceflow.consuming

import no.novari.flyt.kafka.instanceflow.headers.CouldNotReadInstanceFlowHeadersException
import no.novari.flyt.kafka.instanceflow.headers.NoInstanceFlowHeadersException
import no.novari.kafka.consuming.ErrorHandlerConfiguration
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class InstanceFlowErrorHandlerConfigurationMapper(
    private val instanceFlowConsumerRecordMapper: InstanceFlowConsumerRecordMapper,
) {
    fun <VALUE> toErrorHandlerConfiguration(
        instanceFlowErrorHandlerConfiguration: InstanceFlowErrorHandlerConfiguration<VALUE>,
    ): ErrorHandlerConfiguration<VALUE> {
        val builder = ErrorHandlerConfiguration.builder<VALUE>()

        instanceFlowErrorHandlerConfiguration.backOffFunction?.let { backOffFunction ->
            builder.backOffFunction { consumerRecord, exception ->
                Optional.ofNullable(
                    backOffFunction.apply(
                        instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(consumerRecord),
                        exception,
                    ),
                )
            }
        }

        instanceFlowErrorHandlerConfiguration.defaultBackoff?.let(builder::defaultBackoff)

        instanceFlowErrorHandlerConfiguration.customRecoverer?.let { recoverer ->
            builder.customRecoverer {
                consumerRecord,
                _,
                exception,
                ->
                try {
                    recoverer.recover(
                        instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(consumerRecord),
                        exception,
                    )
                } catch (e: NoInstanceFlowHeadersException) {
                    log.warn("Calling recoverer without instance flow headers", e)
                    recoverer.recoverForMissingInstanceFlowHeaders(consumerRecord, exception)
                } catch (e: CouldNotReadInstanceFlowHeadersException) {
                    log.warn("Calling recoverer without instance flow headers", e)
                    recoverer.recoverForMissingInstanceFlowHeaders(consumerRecord, exception)
                }
            }
        }

        return builder
            .classificationType(instanceFlowErrorHandlerConfiguration.classificationType)
            .classificationExceptions(instanceFlowErrorHandlerConfiguration.classificationExceptions)
            .skipRecordOnRecoveryFailure(instanceFlowErrorHandlerConfiguration.skipRecordOnRecoveryFailure)
            .restartRetryOnExceptionChange(instanceFlowErrorHandlerConfiguration.restartRetryOnExceptionChange)
            .restartRetryOnRecoveryFailure(instanceFlowErrorHandlerConfiguration.restartRetryOnRecoveryFailure)
            .build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(InstanceFlowErrorHandlerConfigurationMapper::class.java)
    }
}
