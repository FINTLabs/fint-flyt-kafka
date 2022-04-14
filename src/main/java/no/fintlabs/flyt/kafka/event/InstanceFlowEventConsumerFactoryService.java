package no.fintlabs.flyt.kafka.event;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.event.EventConsumerFactoryService;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicNamePatternParameters;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Service;

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


    public <T> ListenerContainerFactory<T, EventTopicNameParameters, EventTopicNamePatternParameters> createFactory(
            Class<T> valueClass,
            Consumer<InstanceFlowConsumerRecord<T>> consumer,
            CommonErrorHandler errorHandler,
            boolean resetOffsetOnAssignment
    ) {
        return eventConsumerFactoryService.createFactory(
                valueClass,
                consumerRecord -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                errorHandler,
                resetOffsetOnAssignment
        );
    }

}
