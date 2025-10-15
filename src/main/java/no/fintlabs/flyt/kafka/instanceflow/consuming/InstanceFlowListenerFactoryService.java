package no.fintlabs.flyt.kafka.instanceflow.consuming;

import no.fintlabs.kafka.consuming.ListenerConfiguration;
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactory;
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class InstanceFlowListenerFactoryService {
    private final ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowListenerFactoryService(
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        this.parameterizedListenerContainerFactoryService = parameterizedListenerContainerFactoryService;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public <VALUE> ParameterizedListenerContainerFactory<VALUE> createRecordListenerContainerFactory(
            Class<VALUE> valueClass,
            Consumer<InstanceFlowConsumerRecord<VALUE>> recordProcessor,
            ListenerConfiguration listenerConfiguration,
            CommonErrorHandler errorHandler
    ) {
        return parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                valueClass,
                consumerRecord ->
                        recordProcessor.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                listenerConfiguration,
                errorHandler
        );
    }

    public <VALUE extends E_VALUE, E_VALUE> ParameterizedListenerContainerFactory<VALUE> createBatchListenerContainerFactory(
            Class<VALUE> valueClass,
            Consumer<List<InstanceFlowConsumerRecord<VALUE>>> batchProcessor,
            ListenerConfiguration listenerConfiguration,
            CommonErrorHandler errorHandler
    ) {
        return parameterizedListenerContainerFactoryService.createBatchListenerContainerFactory(
                valueClass,
                consumerRecords ->
                        batchProcessor.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecords(consumerRecords)),
                listenerConfiguration,
                errorHandler
        );
    }

}
