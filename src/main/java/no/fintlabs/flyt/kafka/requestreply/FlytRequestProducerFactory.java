package no.fintlabs.flyt.kafka.requestreply;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.common.FintTemplateFactory;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicMappingService;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicMappingService;
import org.springframework.stereotype.Service;

@Service
public class FlytRequestProducerFactory extends RequestProducerFactory {

    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public FlytRequestProducerFactory(
            FintTemplateFactory fintTemplateFactory,
            RequestTopicMappingService requestTopicMappingService,
            ReplyTopicMappingService replyTopicMappingService,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        super(fintTemplateFactory, requestTopicMappingService, replyTopicMappingService);
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public <V, R> InstanceFlowRequestProducer<V, R> createInstanceFlowProducer(
            ReplyTopicNameParameters replyTopicNameParameters,
            Class<V> requestValueClass,
            Class<R> replyValueClass
    ) {
        return new InstanceFlowRequestProducer<>(
                createProducer(replyTopicNameParameters, requestValueClass, replyValueClass),
                instanceFlowHeadersMapper,
                instanceFlowConsumerRecordMapper
        );
    }

}
