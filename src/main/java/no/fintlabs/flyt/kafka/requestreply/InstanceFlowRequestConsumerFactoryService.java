package no.fintlabs.flyt.kafka.requestreply;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNamePatternParameters;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class InstanceFlowRequestConsumerFactoryService {

    private final RequestConsumerFactoryService requestConsumerFactoryService;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowRequestConsumerFactoryService(
            RequestConsumerFactoryService requestConsumerFactoryService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        this.requestConsumerFactoryService = requestConsumerFactoryService;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <V, R> ListenerContainerFactory<V, RequestTopicNameParameters, RequestTopicNamePatternParameters> createFactory(
            Class<V> valueClass,
            Class<R> replyValueClass,
            Function<InstanceFlowConsumerRecord<V>, InstanceFlowReplyProducerRecord<R>> replyFunction,
            CommonErrorHandler errorHandler
    ) {
        return requestConsumerFactoryService.createFactory(
                valueClass,
                replyValueClass,
                consumerRecord -> {
                    InstanceFlowConsumerRecord<V> instanceFlowConsumerRecord = instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord);
                    InstanceFlowReplyProducerRecord<R> instanceFlowReplyProducerRecord = replyFunction.apply(instanceFlowConsumerRecord);
                    return ReplyProducerRecord.<R>builder()
                            .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowReplyProducerRecord.getInstanceFlowHeaders()))
                            .value(instanceFlowReplyProducerRecord.getValue())
                            .build();
                },
                errorHandler
        );
    }

}
