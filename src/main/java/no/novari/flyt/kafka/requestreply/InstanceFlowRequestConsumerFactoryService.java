package no.novari.flyt.kafka.requestreply;

import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord;
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecordMapper;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper;
import no.novari.kafka.requestreply.ReplyProducerRecord;
import no.novari.kafka.requestreply.RequestListenerConfiguration;
import no.novari.kafka.requestreply.RequestListenerContainerFactory;
import no.novari.kafka.requestreply.topic.name.RequestTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class InstanceFlowRequestConsumerFactoryService {

    private final RequestListenerContainerFactory requestListenerContainerFactory;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowRequestConsumerFactoryService(
            RequestListenerContainerFactory requestListenerContainerFactory,
            InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        this.requestListenerContainerFactory = requestListenerContainerFactory;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <V, R> ConcurrentMessageListenerContainer<String, V> createRecordConsumerContainer(
            RequestTopicNameParameters requestTopicNameParameters,
            Class<V> valueClass,
            Class<R> replyValueClass,
            Function<InstanceFlowConsumerRecord<V>, InstanceFlowReplyProducerRecord<R>> replyFunction,
            RequestListenerConfiguration<V> listenerConfiguration,
            CommonErrorHandler errorHandler
    ) {
        return requestListenerContainerFactory.createRecordConsumerFactory(
                valueClass,
                replyValueClass,
                (ConsumerRecord<String, V> consumerRecord) -> {
                    InstanceFlowConsumerRecord<V> instanceFlowConsumerRecord =
                            instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord);
                    InstanceFlowReplyProducerRecord<R> instanceFlowReplyProducerRecord =
                            replyFunction.apply(instanceFlowConsumerRecord);
                    return new ReplyProducerRecord<>(
                            instanceFlowReplyProducerRecord.getInstanceFlowHeaders() != null
                                    ? instanceFlowHeadersMapper.toHeaders(instanceFlowReplyProducerRecord.getInstanceFlowHeaders())
                                    : null,
                            instanceFlowReplyProducerRecord.getValue()
                    );
                },
                listenerConfiguration,
                errorHandler
        ).createContainer(requestTopicNameParameters);
    }

}
