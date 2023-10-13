package no.fintlabs.flyt.kafka.event;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.event.EventConsumerConfiguration;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class InstanceFlowEventConsumerFactoryService {

    private final EventConsumerFactoryService eventConsumerFactoryService;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowEventConsumerFactoryService(
            EventConsumerFactoryService eventConsumerFactoryService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        this.eventConsumerFactoryService = eventConsumerFactoryService;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }


    public <T> ListenerContainerFactory<T, EventTopicNameParameters, EventTopicNamePatternParameters> createRecordFactory(
            Class<T> valueClass,
            Consumer<InstanceFlowConsumerRecord<T>> consumer
    ) {
        return createRecordFactory(valueClass, consumer, EventConsumerConfiguration.empty());
    }

    public <T> ListenerContainerFactory<T, EventTopicNameParameters, EventTopicNamePatternParameters> createRecordFactory(
            Class<T> valueClass,
            Consumer<InstanceFlowConsumerRecord<T>> consumer,
            EventConsumerConfiguration eventConsumerConfiguration
    ) {
        return eventConsumerFactoryService.createRecordConsumerFactory(
                valueClass,
                consumerRecord -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                eventConsumerConfiguration
        );
    }

    public <T> ListenerContainerFactory<T, EventTopicNameParameters, EventTopicNamePatternParameters> createBatchFactory(
            Class<T> valueClass,
            Consumer<List<InstanceFlowConsumerRecord<T>>> consumer
    ) {
        return createBatchFactory(valueClass, consumer, EventConsumerConfiguration.empty());
    }

    public <T> ListenerContainerFactory<T, EventTopicNameParameters, EventTopicNamePatternParameters> createBatchFactory(
            Class<T> valueClass,
            Consumer<List<InstanceFlowConsumerRecord<T>>> consumer,
            EventConsumerConfiguration eventConsumerConfiguration
    ) {
        return eventConsumerFactoryService.createBatchConsumerFactory(
                valueClass,
                consumerRecords -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecords(consumerRecords)),
                eventConsumerConfiguration
        );
    }

}
