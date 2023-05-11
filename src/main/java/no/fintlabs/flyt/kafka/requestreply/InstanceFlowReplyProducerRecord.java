package no.fintlabs.flyt.kafka.requestreply;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder
public class InstanceFlowReplyProducerRecord<T> {
    private final InstanceFlowHeaders instanceFlowHeaders;
    private final T value;
}
