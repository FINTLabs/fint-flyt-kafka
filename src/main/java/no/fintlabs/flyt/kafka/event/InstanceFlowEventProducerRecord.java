package no.fintlabs.flyt.kafka.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class InstanceFlowEventProducerRecord<T> {
    private EventTopicNameParameters topicNameParameters;
    private InstanceFlowHeaders instanceFlowHeaders;
    private String key;
    private T value;
}
