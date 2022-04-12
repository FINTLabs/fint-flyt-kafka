package no.fintlabs.flyt.kafka.event;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.event.EventProducer;
import no.fintlabs.kafka.event.EventProducerRecord;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

public class InstanceFlowEventProducer<T> {

    private final EventProducer<T> eventProducer;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowEventProducer(EventProducer<T> eventProducer, InstanceFlowHeadersMapper instanceFlowHeadersMapper) {
        this.eventProducer = eventProducer;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public ListenableFuture<SendResult<String, T>> send(InstanceFlowEventProducerRecord<T> instanceFlowEventProducerRecord) {
        return eventProducer.send(
                EventProducerRecord.<T>builder()
                        .topicNameParameters(instanceFlowEventProducerRecord.getTopicNameParameters())
                        .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowEventProducerRecord.getInstanceFlowHeaders()))
                        .key(instanceFlowEventProducerRecord.getKey())
                        .value(instanceFlowEventProducerRecord.getValue())
                        .build()
        );
    }

}
