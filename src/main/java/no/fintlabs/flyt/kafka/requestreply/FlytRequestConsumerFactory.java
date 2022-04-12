package no.fintlabs.flyt.kafka.requestreply;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.common.FintTemplateFactory;
import no.fintlabs.kafka.common.ListenerContainerFactory;
import no.fintlabs.kafka.common.ListenerContainerFactoryService;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicMappingService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNamePatternParameters;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class FlytRequestConsumerFactory extends RequestConsumerFactoryService {

    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public FlytRequestConsumerFactory(
            ListenerContainerFactoryService fintListenerContainerFactoryService,
            FintTemplateFactory fintTemplateFactory,
            RequestTopicMappingService requestTopicMappingService,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        super(fintListenerContainerFactoryService, fintTemplateFactory, requestTopicMappingService);
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <V, R> ListenerContainerFactory<V, RequestTopicNameParameters, RequestTopicNamePatternParameters> createInstanceFlowFactory(
            Class<V> valueClass,
            Class<R> replyValueClass,
            Function<InstanceFlowConsumerRecord<V>, InstanceFlowReplyProducerRecord<R>> replyFunction,
            CommonErrorHandler errorHandler
    ) {
        return createFactory(
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
