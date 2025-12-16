package no.novari.flyt.kafka.instanceflow.consuming;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Builder
@Getter
@EqualsAndHashCode
public final class InstanceFlowConsumerRecord<T> {
    private final InstanceFlowHeaders instanceFlowHeaders;
    private final ConsumerRecord<String, T> consumerRecord;
}
