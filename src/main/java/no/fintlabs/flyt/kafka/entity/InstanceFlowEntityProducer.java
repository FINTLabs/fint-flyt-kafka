package no.fintlabs.flyt.kafka.entity;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.entity.EntityProducer;
import no.fintlabs.kafka.entity.EntityProducerRecord;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

public class InstanceFlowEntityProducer<T> {

    private final EntityProducer<T> entityProducer;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowEntityProducer(EntityProducer<T> entityProducer, InstanceFlowHeadersMapper instanceFlowHeadersMapper) {
        this.entityProducer = entityProducer;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public ListenableFuture<SendResult<String, T>> send(InstanceFlowEntityProducerRecord<T> instanceFlowEntityProducerRecord) {
        return entityProducer.send(
                EntityProducerRecord.<T>builder()
                        .topicNameParameters(instanceFlowEntityProducerRecord.getTopicNameParameters())
                        .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowEntityProducerRecord.getInstanceFlowHeaders()))
                        .key(instanceFlowEntityProducerRecord.getKey())
                        .value(instanceFlowEntityProducerRecord.getValue())
                        .build()
        );
    }

}
