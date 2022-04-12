package no.fintlabs.flyt.kafka.requestreply;

import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.InstanceFlowConsumerRecordMapper;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;

import java.util.Optional;
import java.util.function.Consumer;

public class InstanceFlowRequestProducer<V, R> {

    private final RequestProducer<V, R> requestProducer;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;
    private final InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;

    public InstanceFlowRequestProducer(RequestProducer<V, R> requestProducer, InstanceFlowHeadersMapper instanceFlowHeadersMapper, InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper) {
        this.requestProducer = requestProducer;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
        this.instanceFlowConsumerRecordMapper = instanceFlowConsumerRecordMapper;
    }

    public Optional<InstanceFlowConsumerRecord<R>> requestAndReceive(InstanceFlowRequestProducerRecord<V> instanceFlowRequestProducerRecord) {
        return requestProducer.requestAndReceive(toRequestProducerRecord(instanceFlowRequestProducerRecord))
                .map(instanceFlowConsumerRecordMapper::toFlytConsumerRecord);
    }

    public void requestWithAsyncReplyConsumer(
            InstanceFlowRequestProducerRecord<V> instanceFlowRequestProducerRecord,
            Consumer<InstanceFlowConsumerRecord<R>> replyConsumer,
            Consumer<Throwable> failureConsumer
    ) {
        requestProducer.requestWithAsyncReplyConsumer(
                toRequestProducerRecord(instanceFlowRequestProducerRecord),
                consumerRecord -> replyConsumer.accept(instanceFlowConsumerRecordMapper.toFlytConsumerRecord(consumerRecord)),
                failureConsumer
        );
    }

    private RequestProducerRecord<V> toRequestProducerRecord(InstanceFlowRequestProducerRecord<V> instanceFlowRequestProducerRecord) {
        return RequestProducerRecord
                .<V>builder()
                .topicNameParameters(instanceFlowRequestProducerRecord.getTopicNameParameters())
                .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowRequestProducerRecord.getInstanceFlowHeaders()))
                .value(instanceFlowRequestProducerRecord.getValue())
                .build();
    }

}
