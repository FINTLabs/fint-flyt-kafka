package no.fintlabs.flyt.kafka.requestreply;

import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;

public record InstanceFlowReplyProducerRecord<T>(InstanceFlowHeaders instanceFlowHeaders, T value) {
}
