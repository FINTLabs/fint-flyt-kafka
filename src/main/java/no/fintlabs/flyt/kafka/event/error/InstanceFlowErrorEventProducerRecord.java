package no.fintlabs.flyt.kafka.event.error;

import lombok.Builder;
import lombok.Data;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;

@Data
@Builder
public class InstanceFlowErrorEventProducerRecord {
    private ErrorEventTopicNameParameters topicNameParameters;
    private InstanceFlowHeaders instanceFlowHeaders;
    private ErrorCollection errorCollection;
}
