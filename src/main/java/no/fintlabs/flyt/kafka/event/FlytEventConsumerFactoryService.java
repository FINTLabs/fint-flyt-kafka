package no.fintlabs.flyt.kafka.event;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.common.ListenerContainerFactoryService;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicMappingService;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class FlytEventConsumerFactoryService extends EventConsumerFactoryService {

    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public FlytEventConsumerFactoryService(
            ListenerContainerFactoryService fintListenerContainerFactoryService,
            EventTopicMappingService eventTopicMappingService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        super(fintListenerContainerFactoryService, eventTopicMappingService);
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public <T> ListenerContainerFactory<T, EventTopicNameParameters, EventTopicNamePatternParameters> createInstanceFlowFactory(
            Class<T> valueClass,
            Consumer<InstanceFlowConsumerRecord<T>> consumer,
            CommonErrorHandler errorHandler,
            boolean resetOffsetOnAssignment
    ) {
        return createFactory(
                valueClass,
                consumerRecord -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                errorHandler,
                resetOffsetOnAssignment
        );
    }

}
