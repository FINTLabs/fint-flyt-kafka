package no.fintlabs.flyt.kafka.requestreply;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;

public record InstanceFlowReplyProducerRecord<T>(InstanceFlowHeaders instanceFlowHeaders, T value) {
}
