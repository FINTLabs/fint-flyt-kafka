package no.fintlabs.flyt.kafka.requestreply;

import lombok.Builder;
import lombok.Data;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;

@Data
@Builder
public class InstanceFlowRequestProducerRecord<T> {
    private RequestTopicNameParameters topicNameParameters;
    private InstanceFlowHeaders instanceFlowHeaders;
    private T value;
}
