package no.fintlabs.flyt.kafka.requestreply;

import lombok.Builder;
import lombok.Getter;
import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;

@Builder
@Getter
public final class InstanceFlowReplyProducerRecord<T> {
    private final InstanceFlowHeaders instanceFlowHeaders;
    private final T value;
}
