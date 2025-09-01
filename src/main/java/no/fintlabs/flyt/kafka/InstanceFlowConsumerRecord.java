package no.fintlabs.flyt.kafka;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public record InstanceFlowConsumerRecord<T>(InstanceFlowHeaders instanceFlowHeaders,
                                            ConsumerRecord<String, T> consumerRecord) {
}
