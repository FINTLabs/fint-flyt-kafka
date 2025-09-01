package no.fintlabs.flyt.kafka.event;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.consuming.ErrorHandlerConfiguration;
import no.fintlabs.kafka.consuming.ListenerConfiguration;
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactory;
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class InstanceFlowEventConsumerFactoryService {

    private final ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowEventConsumerFactoryService(
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        this.parameterizedListenerContainerFactoryService = parameterizedListenerContainerFactoryService;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }


    public <T> ParameterizedListenerContainerFactory<T> createRecordFactory(
            Class<T> valueClass,
            Consumer<InstanceFlowConsumerRecord<T>> consumer
    ) {
        var defaultConfig = ListenerConfiguration
                .builder(valueClass)
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .errorHandler(
                        ErrorHandlerConfiguration
                                .builder(valueClass)
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                )
                .continueFromPreviousOffsetOnAssignment()
                .build();
        return createRecordFactory(consumer, defaultConfig);
    }

    public <T> ParameterizedListenerContainerFactory<T> createRecordFactory(
            Consumer<InstanceFlowConsumerRecord<T>> consumer,
            ListenerConfiguration<T> listenerConfiguration
    ) {
        return parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                consumerRecord -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                listenerConfiguration
        );
    }

    public <T> ParameterizedListenerContainerFactory<T> createBatchFactory(
            Class<T> valueClass,
            Consumer<List<InstanceFlowConsumerRecord<T>>> consumer
    ) {
        var defaultConfig = ListenerConfiguration
                .builder(valueClass)
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .errorHandler(
                        ErrorHandlerConfiguration
                                .builder(valueClass)
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                )
                .continueFromPreviousOffsetOnAssignment()
                .build();
        return createBatchFactory(consumer, defaultConfig);
    }

    public <T> ParameterizedListenerContainerFactory<T> createBatchFactory(
            Consumer<List<InstanceFlowConsumerRecord<T>>> consumer,
            ListenerConfiguration<T> listenerConfiguration
    ) {
        return parameterizedListenerContainerFactoryService.createBatchListenerContainerFactory(
                consumerRecords -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecords(consumerRecords)),
                listenerConfiguration
        );
    }

}
