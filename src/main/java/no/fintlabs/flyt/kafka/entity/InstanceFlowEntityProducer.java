package no.fintlabs.flyt.kafka.entity;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.model.ParameterizedProducerRecord;
import no.fintlabs.kafka.producing.ParameterizedTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public record InstanceFlowEntityProducer<T>(
        ParameterizedTemplate<T> parameterizedTemplate,
        InstanceFlowHeadersMapper instanceFlowHeadersMapper) {

    public CompletableFuture<SendResult<String, T>> send(
            InstanceFlowEntityProducerRecord<T> instanceFlowEntityProducerRecord) {
        return parameterizedTemplate.send(
                ParameterizedProducerRecord.<T>builder()
                        .topicNameParameters(instanceFlowEntityProducerRecord.getTopicNameParameters())
                        .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowEntityProducerRecord.getInstanceFlowHeaders()))
                        .key(instanceFlowEntityProducerRecord.getKey())
                        .value(instanceFlowEntityProducerRecord.getValue())
                        .build()
        );
    }

}
