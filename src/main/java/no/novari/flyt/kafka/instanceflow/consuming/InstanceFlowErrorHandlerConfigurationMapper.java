package no.novari.flyt.kafka.instanceflow.consuming;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.kafka.instanceflow.headers.CouldNotReadInstanceFlowHeadersException;
import no.novari.flyt.kafka.instanceflow.headers.NoInstanceFlowHeadersException;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InstanceFlowErrorHandlerConfigurationMapper {

    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowErrorHandlerConfigurationMapper(
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public <VALUE> ErrorHandlerConfiguration<VALUE> toErrorHandlerConfiguration(
            InstanceFlowErrorHandlerConfiguration<VALUE> instanceFlowErrorHandlerConfiguration
    ) {
        ErrorHandlerConfiguration.ErrorHandlerConfigurationBuilder<VALUE>
                errorHandlerConfigurationBuilder = ErrorHandlerConfiguration.builder();

        instanceFlowErrorHandlerConfiguration
                .getBackOffFunction()
                .ifPresent(backOffFunction ->
                        errorHandlerConfigurationBuilder.backOffFunction(
                                (consumerRecord, exception) ->
                                        backOffFunction.apply(
                                                instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(
                                                        consumerRecord),
                                                exception
                                        )
                        )
                );

        instanceFlowErrorHandlerConfiguration
                .getDefaultBackoff()
                .ifPresent(errorHandlerConfigurationBuilder::defaultBackoff);

        instanceFlowErrorHandlerConfiguration
                .getCustomRecoverer()
                .ifPresent(recoverer ->
                        errorHandlerConfigurationBuilder.customRecoverer(
                                (consumerRecord, consumer, exception) -> {
                                    try {
                                        recoverer.recover(
                                                instanceFlowConsumerRecordMapper
                                                        .toInstanceFlowConsumerRecord(consumerRecord),
                                                exception
                                        );
                                    } catch (NoInstanceFlowHeadersException |
                                             CouldNotReadInstanceFlowHeadersException e) {
                                        log.warn("Calling recoverer without instance flow headers", e);
                                        recoverer.recoverForMissingInstanceFlowHeaders(
                                                consumerRecord,
                                                exception
                                        );
                                    }
                                }
                        )
                );

        return errorHandlerConfigurationBuilder
                .classificationType(instanceFlowErrorHandlerConfiguration.getClassificationType())
                .classificationExceptions(instanceFlowErrorHandlerConfiguration.getClassificationExceptions())
                .skipRecordOnRecoveryFailure(instanceFlowErrorHandlerConfiguration.isSkipRecordOnRecoveryFailure())
                .restartRetryOnExceptionChange(instanceFlowErrorHandlerConfiguration.isRestartRetryOnExceptionChange())
                .restartRetryOnRecoveryFailure(instanceFlowErrorHandlerConfiguration.isRestartRetryOnRecoveryFailure())
                .build();
    }

}
