package no.fintlabs.flyt.kafka.event.error;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.model.ErrorCollection;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class InstanceFlowErrorEventProducerRecord {
    private no.fintlabs.kafka.topic.name.ErrorEventTopicNameParameters topicNameParameters;
    private InstanceFlowHeaders instanceFlowHeaders;
    private ErrorCollection errorCollection;
}
