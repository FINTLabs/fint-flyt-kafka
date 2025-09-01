package no.fintlabs.flyt.kafka.event;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.topic.name.EventTopicNameParameters;

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
