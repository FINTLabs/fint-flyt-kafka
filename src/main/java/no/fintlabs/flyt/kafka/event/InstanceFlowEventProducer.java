package no.fintlabs.flyt.kafka.event;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.model.ParameterizedProducerRecord;
import no.fintlabs.kafka.producing.ParameterizedTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public record InstanceFlowEventProducer<T>(ParameterizedTemplate<T> parameterizedTemplate, InstanceFlowHeadersMapper instanceFlowHeadersMapper) {

    public CompletableFuture<SendResult<String, T>> send(
            InstanceFlowEventProducerRecord<T> instanceFlowEventProducerRecord) {
        return parameterizedTemplate.send(
                ParameterizedProducerRecord.<T>builder()
                        .topicNameParameters(instanceFlowEventProducerRecord.getTopicNameParameters())
                        .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowEventProducerRecord.getInstanceFlowHeaders()))
                        .key(instanceFlowEventProducerRecord.getKey())
                        .value(instanceFlowEventProducerRecord.getValue())
                        .build()
        );
    }

}
