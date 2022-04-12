package no.fintlabs.flyt.kafka.event;

import lombok.Builder;
import lombok.Data;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;

@Data
@Builder
public class InstanceFlowEventProducerRecord<T> {
    private EventTopicNameParameters topicNameParameters;
    private InstanceFlowHeaders instanceFlowHeaders;
    private String key;
    private T value;
}
