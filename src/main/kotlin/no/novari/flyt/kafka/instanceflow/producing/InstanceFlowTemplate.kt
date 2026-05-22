package no.novari.flyt.kafka.instanceflow.producing

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper
import no.novari.kafka.producing.ParameterizedProducerRecord
import no.novari.kafka.producing.ParameterizedTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

data class InstanceFlowTemplate<VALUE>(
    val parameterizedTemplate: ParameterizedTemplate<VALUE>,
    val instanceFlowHeadersMapper: InstanceFlowHeadersMapper,
) {
    fun send(
        instanceFlowProducerRecord: InstanceFlowProducerRecord<VALUE>,
    ): CompletableFuture<SendResult<String, VALUE>> =
        parameterizedTemplate.send(
            ParameterizedProducerRecord
                .builder<VALUE>()
                .topicNameParameters(instanceFlowProducerRecord.topicNameParameters)
                .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowProducerRecord.instanceFlowHeaders))
                .key(instanceFlowProducerRecord.key)
                .value(instanceFlowProducerRecord.value)
                .build(),
        )
}
