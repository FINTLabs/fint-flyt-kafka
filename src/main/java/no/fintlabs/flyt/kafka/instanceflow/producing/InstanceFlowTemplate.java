package no.fintlabs.flyt.kafka.instanceflow.producing;

import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.model.ParameterizedProducerRecord;
import no.fintlabs.kafka.producing.ParameterizedTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public record InstanceFlowTemplate<VALUE>(
        ParameterizedTemplate<VALUE> parameterizedTemplate,
        InstanceFlowHeadersMapper instanceFlowHeadersMapper
) {

    public CompletableFuture<SendResult<String, VALUE>> send(
            InstanceFlowProducerRecord<VALUE> instanceFlowProducerRecord
    ) {
        return parameterizedTemplate.send(
                ParameterizedProducerRecord.<VALUE>builder()
                        .topicNameParameters(instanceFlowProducerRecord.getTopicNameParameters())
                        .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowProducerRecord.getInstanceFlowHeaders()))
                        .key(instanceFlowProducerRecord.getKey())
                        .value(instanceFlowProducerRecord.getValue())
                        .build()
        );
    }

}
