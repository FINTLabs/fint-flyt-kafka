package no.fintlabs.flyt.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Data
@AllArgsConstructor
public class InstanceFlowConsumerRecord<T> {
    private final InstanceFlowHeaders instanceFlowHeaders;
    private final ConsumerRecord<String, T> consumerRecord;
}
