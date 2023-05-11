package no.fintlabs.flyt.kafka.event.error;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class InstanceFlowErrorEventProducerRecord {
    private ErrorEventTopicNameParameters topicNameParameters;
    private InstanceFlowHeaders instanceFlowHeaders;
    private ErrorCollection errorCollection;
}
