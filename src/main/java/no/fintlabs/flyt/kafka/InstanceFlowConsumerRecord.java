package no.fintlabs.flyt.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class InstanceFlowConsumerRecord<T> {
    private final InstanceFlowHeaders instanceFlowHeaders;
    private final ConsumerRecord<String, T> consumerRecord;
}
