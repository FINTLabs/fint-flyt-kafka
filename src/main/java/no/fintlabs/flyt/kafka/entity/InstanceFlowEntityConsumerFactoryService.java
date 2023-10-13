package no.fintlabs.flyt.kafka.entity;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.entity.EntityConsumerConfiguration;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicNamePatternParameters;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class InstanceFlowEntityConsumerFactoryService {

    private final EntityConsumerFactoryService entityConsumerFactoryService;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowEntityConsumerFactoryService(
            EntityConsumerFactoryService entityConsumerFactoryService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        this.entityConsumerFactoryService = entityConsumerFactoryService;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public <T> ListenerContainerFactory<T, EntityTopicNameParameters, EntityTopicNamePatternParameters> createRecordFactory(
            Class<T> valueClass,
            Consumer<InstanceFlowConsumerRecord<T>> consumer
    ) {
        return createRecordFactory(valueClass, consumer, EntityConsumerConfiguration.empty());
    }

    public <T> ListenerContainerFactory<T, EntityTopicNameParameters, EntityTopicNamePatternParameters> createRecordFactory(
            Class<T> valueClass,
            Consumer<InstanceFlowConsumerRecord<T>> consumer,
            EntityConsumerConfiguration entityConsumerConfiguration
    ) {
        return entityConsumerFactoryService.createRecordConsumerFactory(
                valueClass,
                consumerRecord -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                entityConsumerConfiguration
        );
    }

    public <T> ListenerContainerFactory<T, EntityTopicNameParameters, EntityTopicNamePatternParameters> createBatchFactory(
            Class<T> valueClass,
            Consumer<List<InstanceFlowConsumerRecord<T>>> consumer
    ) {
        return createBatchFactory(valueClass, consumer, EntityConsumerConfiguration.empty());
    }

    public <T> ListenerContainerFactory<T, EntityTopicNameParameters, EntityTopicNamePatternParameters> createBatchFactory(
            Class<T> valueClass,
            Consumer<List<InstanceFlowConsumerRecord<T>>> consumer,
            EntityConsumerConfiguration entityConsumerConfiguration
    ) {
        return entityConsumerFactoryService.createBatchConsumerFactory(
                valueClass,
                consumerRecords -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecords(consumerRecords)),
                entityConsumerConfiguration
        );
    }


}
