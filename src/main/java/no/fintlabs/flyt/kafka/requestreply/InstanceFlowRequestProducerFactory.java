package no.fintlabs.flyt.kafka.requestreply;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowRequestProducerFactory {

    private final RequestProducerFactory requestProducerFactory;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowRequestProducerFactory(
            RequestProducerFactory requestProducerFactory,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper
    ) {
        this.requestProducerFactory = requestProducerFactory;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public <V, R> InstanceFlowRequestProducer<V, R> createProducer(
            ReplyTopicNameParameters replyTopicNameParameters,
            Class<V> requestValueClass,
            Class<R> replyValueClass
    ) {
        return new InstanceFlowRequestProducer<>(
                requestProducerFactory.createProducer(replyTopicNameParameters, requestValueClass, replyValueClass),
                instanceFlowHeadersMapper,
                instanceFlowConsumerRecordMapper
        );
    }

}
