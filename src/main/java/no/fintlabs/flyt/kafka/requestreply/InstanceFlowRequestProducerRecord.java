package no.fintlabs.flyt.kafka.requestreply;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class InstanceFlowRequestProducerRecord<T> {
    private RequestTopicNameParameters topicNameParameters;
    private InstanceFlowHeaders instanceFlowHeaders;
    private T value;
}
