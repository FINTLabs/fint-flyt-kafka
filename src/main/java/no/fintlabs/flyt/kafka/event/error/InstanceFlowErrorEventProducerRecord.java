package no.fintlabs.flyt.kafka.event.error;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.model.ErrorCollection;
import no.fintlabs.kafka.topic.name.ErrorEventTopicNameParameters;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class InstanceFlowErrorEventProducerRecord {
    private ErrorEventTopicNameParameters topicNameParameters;
    private InstanceFlowHeaders instanceFlowHeaders;
    private ErrorCollection errorCollection;
}
