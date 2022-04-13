package no.fintlabs.flyt.kafka.entity;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.common.ListenerContainerFactoryService;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicMappingService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.entity.topic.EntityTopicNamePatternParameters;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class FlytEntityConsumerFactoryService extends EntityConsumerFactoryService {

    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public FlytEntityConsumerFactoryService(
            ListenerContainerFactoryService fintListenerContainerFactoryService,
            EntityTopicMappingService entityTopicMappingService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        super(fintListenerContainerFactoryService, entityTopicMappingService);
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }


    public <T> ListenerContainerFactory<T, EntityTopicNameParameters, EntityTopicNamePatternParameters> createInstanceFlowFactory(
            Class<T> valueClass,
            Consumer<InstanceFlowConsumerRecord<T>> consumer,
            CommonErrorHandler errorHandler
    ) {
        return createFactory(
                valueClass,
                consumerRecord -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                errorHandler
        );
    }

}
