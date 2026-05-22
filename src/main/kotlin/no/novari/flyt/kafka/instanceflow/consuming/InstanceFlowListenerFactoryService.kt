package no.novari.flyt.kafka.instanceflow.consuming

import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.consuming.ParameterizedListenerContainerFactory
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class InstanceFlowListenerFactoryService(
    private val parameterizedListenerContainerFactoryService: ParameterizedListenerContainerFactoryService,
    private val instanceFlowConsumerRecordMapper: InstanceFlowConsumerRecordMapper,
) {
    fun <VALUE> createRecordListenerContainerFactory(
        valueClass: Class<VALUE>,
        recordProcessor: Consumer<InstanceFlowConsumerRecord<VALUE>>,
        listenerConfiguration: ListenerConfiguration,
        errorHandler: CommonErrorHandler?,
    ): ParameterizedListenerContainerFactory<VALUE> =
        parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
            valueClass,
            { consumerRecord ->
                recordProcessor.accept(
                    instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(consumerRecord),
                )
            },
            listenerConfiguration,
            errorHandler,
        )

    fun <VALUE : E_VALUE, E_VALUE> createBatchListenerContainerFactory(
        valueClass: Class<VALUE>,
        batchProcessor: Consumer<List<InstanceFlowConsumerRecord<VALUE>>>,
        listenerConfiguration: ListenerConfiguration,
        errorHandler: CommonErrorHandler?,
    ): ParameterizedListenerContainerFactory<VALUE> =
        parameterizedListenerContainerFactoryService.createBatchListenerContainerFactory(
            valueClass,
            { consumerRecords ->
                batchProcessor.accept(
                    instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecords(consumerRecords),
                )
            },
            listenerConfiguration,
            errorHandler,
        )
}
