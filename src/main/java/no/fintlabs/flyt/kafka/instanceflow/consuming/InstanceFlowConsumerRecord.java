package no.fintlabs.flyt.kafka.instanceflow.consuming;

import lombok.Builder;
import lombok.Getter;
import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Builder
@Getter
public final class InstanceFlowConsumerRecord<T> {
    private final InstanceFlowHeaders instanceFlowHeaders;
    private final ConsumerRecord<String, T> consumerRecord;
}
