package no.fintlabs.flyt.kafka.requestreply;

import lombok.Builder;
import lombok.Data;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;

@Data
@Builder
public class InstanceFlowReplyProducerRecord<T> {
    private final InstanceFlowHeaders instanceFlowHeaders;
    private final T value;
}
