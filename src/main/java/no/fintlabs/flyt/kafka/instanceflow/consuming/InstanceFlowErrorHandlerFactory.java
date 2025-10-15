package no.fintlabs.flyt.kafka.instanceflow.consuming;

import no.fintlabs.kafka.consuming.ErrorHandlerConfiguration;
import no.fintlabs.kafka.consuming.ErrorHandlerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowErrorHandlerFactory {

    private final ErrorHandlerFactory errorHandlerFactory;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowErrorHandlerFactory(
            ErrorHandlerFactory errorHandlerFactory,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        this.errorHandlerFactory = errorHandlerFactory;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public <VALUE> DefaultErrorHandler createErrorHandler(
            InstanceFlowErrorHandlerConfiguration<VALUE> instanceFlowErrorHandlerConfiguration
    ) {
        ErrorHandlerConfiguration.ErrorHandlerConfigurationBuilder<VALUE> errorHandlerConfigurationBuilder =
                ErrorHandlerConfiguration.builder();

        instanceFlowErrorHandlerConfiguration.getBackOffFunction().ifPresent(backOffFunction ->
                errorHandlerConfigurationBuilder.backOffFunction(
                        (consumerRecord, exception) -> backOffFunction
                                .apply(
                                        instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord),
                                        exception
                                )
                )
        );

        instanceFlowErrorHandlerConfiguration.getDefaultBackoff()
                .ifPresent(errorHandlerConfigurationBuilder::defaultBackoff);

        instanceFlowErrorHandlerConfiguration.getRecoverer().ifPresent(recoverer ->
                errorHandlerConfigurationBuilder.recoverer(
                        ((consumerRecord, consumer, exception) ->
                                recoverer.accept(
                                        instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord),
                                        consumer,
                                        exception
                                ))
                )
        );

        return errorHandlerFactory.createErrorHandler(errorHandlerConfigurationBuilder.build());
    }

}
