package no.fintlabs.flyt.kafka.instanceflow.consuming;

import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public record InstanceFlowConsumerRecord<T>(InstanceFlowHeaders instanceFlowHeaders,
                                            ConsumerRecord<String, T> consumerRecord) {
}
