package no.novari.flyt.kafka.instanceflow.consuming;

import no.novari.kafka.consuming.ErrorHandlerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowErrorHandlerFactory {

    private final ErrorHandlerFactory errorHandlerFactory;
    private final InstanceFlowErrorHandlerConfigurationMapper instanceFlowErrorHandlerConfigurationMapper;

    public InstanceFlowErrorHandlerFactory(
            ErrorHandlerFactory errorHandlerFactory,
            InstanceFlowErrorHandlerConfigurationMapper instanceFlowErrorHandlerConfigurationMapper
    ) {
        this.errorHandlerFactory = errorHandlerFactory;
        this.instanceFlowErrorHandlerConfigurationMapper = instanceFlowErrorHandlerConfigurationMapper;
    }

    public <VALUE> DefaultErrorHandler createErrorHandler(
            InstanceFlowErrorHandlerConfiguration<VALUE> instanceFlowErrorHandlerConfiguration
    ) {
        return errorHandlerFactory.createErrorHandler(
                instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                        instanceFlowErrorHandlerConfiguration
                )
        );
    }

}
