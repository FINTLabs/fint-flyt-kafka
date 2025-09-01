package no.fintlabs.flyt.kafka.event.error;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.consuming.ErrorHandlerConfiguration;
import no.fintlabs.kafka.consuming.ListenerConfiguration;
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactory;
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService;
import no.fintlabs.kafka.model.ErrorCollection;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class InstanceFlowErrorEventConsumerFactoryService {

    private final ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowErrorEventConsumerFactoryService(
            ParameterizedListenerContainerFactoryService parameterizedListenerContainerFactoryService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        this.parameterizedListenerContainerFactoryService = parameterizedListenerContainerFactoryService;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public ParameterizedListenerContainerFactory<ErrorCollection> createRecordFactory(
            Consumer<InstanceFlowConsumerRecord<ErrorCollection>> consumer
    ) {
        ListenerConfiguration<ErrorCollection> defaultConfig = ListenerConfiguration
                .builder(ErrorCollection.class)
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .errorHandler(
                        ErrorHandlerConfiguration
                                .builder(ErrorCollection.class)
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                )
                .continueFromPreviousOffsetOnAssignment()
                .build();
        return createRecordFactory(consumer, defaultConfig);
    }

    public ParameterizedListenerContainerFactory<ErrorCollection> createRecordFactory(
            Consumer<InstanceFlowConsumerRecord<ErrorCollection>> consumer,
            ListenerConfiguration<ErrorCollection> listenerConfiguration
    ) {
        return parameterizedListenerContainerFactoryService.createRecordListenerContainerFactory(
                consumerRecord -> consumer.accept(
                        instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                listenerConfiguration
        );
    }

    public ParameterizedListenerContainerFactory<ErrorCollection> createBatchFactory(
            Consumer<List<InstanceFlowConsumerRecord<ErrorCollection>>> consumer

    ) {
        ListenerConfiguration<ErrorCollection> defaultConfig = ListenerConfiguration
                .builder(ErrorCollection.class)
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .errorHandler(
                        ErrorHandlerConfiguration
                                .builder(ErrorCollection.class)
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                )
                .continueFromPreviousOffsetOnAssignment()
                .build();
        return createBatchFactory(consumer, defaultConfig);
    }

    public ParameterizedListenerContainerFactory<ErrorCollection> createBatchFactory(
            Consumer<List<InstanceFlowConsumerRecord<ErrorCollection>>> consumer,
            ListenerConfiguration<ErrorCollection> listenerConfiguration

    ) {
        return parameterizedListenerContainerFactoryService.createBatchListenerContainerFactory(
                consumerRecords -> consumer.accept(
                        instanceFlowConsumerRecordMapper.toFlytConsumerRecords(consumerRecords)),
                listenerConfiguration
        );
    }

}
