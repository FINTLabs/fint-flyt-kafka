package no.fintlabs.flyt.kafka.instanceflow.producing;

import lombok.Builder;
import lombok.Getter;
import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.topic.name.TopicNameParameters;

@Getter
@Builder
public class InstanceFlowProducerRecord<V> {
    private final TopicNameParameters topicNameParameters;
    private final InstanceFlowHeaders instanceFlowHeaders;
    private final String key;
    private final V value;
}
