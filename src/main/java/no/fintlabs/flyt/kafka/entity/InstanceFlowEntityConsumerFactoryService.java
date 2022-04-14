package no.fintlabs.flyt.kafka.entity;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicNamePatternParameters;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Service;

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

    public <T> ListenerContainerFactory<T, EntityTopicNameParameters, EntityTopicNamePatternParameters> createInstanceFlowFactory(
            Class<T> valueClass,
            Consumer<InstanceFlowConsumerRecord<T>> consumer,
            CommonErrorHandler errorHandler
    ) {
        return entityConsumerFactoryService.createFactory(
                valueClass,
                consumerRecord -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                errorHandler
        );
    }

}
