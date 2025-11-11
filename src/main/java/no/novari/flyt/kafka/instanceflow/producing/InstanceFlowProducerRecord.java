package no.novari.flyt.kafka.instanceflow.producing;

import lombok.Builder;
import lombok.Getter;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.novari.kafka.topic.name.TopicNameParameters;

@Getter
@Builder
public class InstanceFlowProducerRecord<V> {
    private final TopicNameParameters topicNameParameters;
    private final InstanceFlowHeaders instanceFlowHeaders;
    private final String key;
    private final V value;
}
