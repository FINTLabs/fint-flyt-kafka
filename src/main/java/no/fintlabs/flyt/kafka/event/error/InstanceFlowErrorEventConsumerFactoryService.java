package no.fintlabs.flyt.kafka.event.error;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.ErrorEventConsumerConfiguration;
import no.fintlabs.kafka.event.error.ErrorEventConsumerFactoryService;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNamePatternParameters;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public ListenerContainerFactory<ErrorCollection, ErrorEventTopicNameParameters, ErrorEventTopicNamePatternParameters> createRecordFactory(
            Consumer<InstanceFlowConsumerRecord<ErrorCollection>> consumer
    ) {
        return createRecordFactory(consumer, ErrorEventConsumerConfiguration.empty());
    }

    public ListenerContainerFactory<ErrorCollection, ErrorEventTopicNameParameters, ErrorEventTopicNamePatternParameters> createRecordFactory(
            Consumer<InstanceFlowConsumerRecord<ErrorCollection>> consumer,
            ErrorEventConsumerConfiguration errorEventConsumerConfiguration
    ) {
        return errorEventConsumerFactoryService.createRecordConsumerFactory(
                consumerRecord -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                errorEventConsumerConfiguration
        );
    }

    public ListenerContainerFactory<ErrorCollection, ErrorEventTopicNameParameters, ErrorEventTopicNamePatternParameters> createBatchFactory(
            Consumer<List<InstanceFlowConsumerRecord<ErrorCollection>>> consumer

    ) {
        return createBatchFactory(consumer, ErrorEventConsumerConfiguration.empty());
    }

    public ListenerContainerFactory<ErrorCollection, ErrorEventTopicNameParameters, ErrorEventTopicNamePatternParameters> createBatchFactory(
            Consumer<List<InstanceFlowConsumerRecord<ErrorCollection>>> consumer,
            ErrorEventConsumerConfiguration errorEventConsumerConfiguration

    ) {
        return errorEventConsumerFactoryService.createBatchConsumerFactory(
                consumerRecords -> consumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecords(consumerRecords)),
                errorEventConsumerConfiguration
        );
    }

}
