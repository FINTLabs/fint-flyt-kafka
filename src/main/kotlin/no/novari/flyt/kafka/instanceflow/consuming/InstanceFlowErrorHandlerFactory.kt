package no.novari.flyt.kafka.instanceflow.consuming

import no.novari.kafka.consuming.ErrorHandlerFactory
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.stereotype.Service

@Service
class InstanceFlowErrorHandlerFactory(
    private val errorHandlerFactory: ErrorHandlerFactory,
    private val instanceFlowErrorHandlerConfigurationMapper: InstanceFlowErrorHandlerConfigurationMapper,
) {
    fun <VALUE> createErrorHandler(
        instanceFlowErrorHandlerConfiguration: InstanceFlowErrorHandlerConfiguration<VALUE>,
    ): DefaultErrorHandler =
        errorHandlerFactory.createErrorHandler(
            instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                instanceFlowErrorHandlerConfiguration,
            ),
        )
}
