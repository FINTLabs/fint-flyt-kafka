package no.fintlabs.flyt.kafka.event.error;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.ErrorEventConsumerFactoryService;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNamePatternParameters;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class InstanceFlowErrorEventConsumerFactoryService {

    private final ErrorEventConsumerFactoryService errorEventConsumerFactoryService;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowErrorEventConsumerFactoryService(
            ErrorEventConsumerFactoryService errorEventConsumerFactoryService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        this.errorEventConsumerFactoryService = errorEventConsumerFactoryService;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public ListenerContainerFactory<ErrorCollection, ErrorEventTopicNameParameters, ErrorEventTopicNamePatternParameters> createFactory(
            Consumer<InstanceFlowConsumerRecord<ErrorCollection>> consumer,
            CommonErrorHandler errorHandler,
            boolean resetOffsetOnAssignment
    ) {
        return errorEventConsumerFactoryService.createFactory(
                consumerRecord -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                errorHandler,
                resetOffsetOnAssignment
        );
    }

}
