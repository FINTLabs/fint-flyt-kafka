package no.fintlabs.flyt.kafka.requestreply;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.consuming.ErrorHandlerConfiguration;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestListenerConfiguration;
import no.fintlabs.kafka.requestreply.RequestListenerContainerFactory;
import no.fintlabs.kafka.requestreply.topic.name.RequestTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
            Function<InstanceFlowConsumerRecord<V>, InstanceFlowReplyProducerRecord<R>> replyFunction
    ) {
        RequestListenerConfiguration<V> defaultConfig = RequestListenerConfiguration
                .builder(valueClass)
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .errorHandler(
                        ErrorHandlerConfiguration
                                .builder(valueClass)
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                )
                .build();
        return createRecordConsumerContainer(requestTopicNameParameters, valueClass, replyValueClass, replyFunction, defaultConfig);
    }

    public <V, R> ConcurrentMessageListenerContainer<String, V> createRecordConsumerContainer(
            RequestTopicNameParameters requestTopicNameParameters,
            Class<V> valueClass,
            Class<R> replyValueClass,
            Function<InstanceFlowConsumerRecord<V>, InstanceFlowReplyProducerRecord<R>> replyFunction,
            RequestListenerConfiguration<V> listenerConfiguration
    ) {
        return requestListenerContainerFactory.createRecordConsumerFactory(
                requestTopicNameParameters,
                valueClass,
                replyValueClass,
                (ConsumerRecord<String, V> consumerRecord) -> {
                    InstanceFlowConsumerRecord<V> instanceFlowConsumerRecord = instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord);
                    InstanceFlowReplyProducerRecord<R> instanceFlowReplyProducerRecord = replyFunction.apply(instanceFlowConsumerRecord);
                    return new ReplyProducerRecord<>(
                            instanceFlowReplyProducerRecord.instanceFlowHeaders() != null
                                    ? instanceFlowHeadersMapper.toHeaders(instanceFlowReplyProducerRecord.instanceFlowHeaders())
                                    : null,
                            instanceFlowReplyProducerRecord.value()
                    );
                },
                listenerConfiguration
        );
    }

}
