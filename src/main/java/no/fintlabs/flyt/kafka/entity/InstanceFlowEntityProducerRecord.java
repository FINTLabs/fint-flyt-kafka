package no.fintlabs.flyt.kafka.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class InstanceFlowEntityProducerRecord<T> {
    private EntityTopicNameParameters topicNameParameters;
    private InstanceFlowHeaders instanceFlowHeaders;
    private String key;
    private T value;
}
