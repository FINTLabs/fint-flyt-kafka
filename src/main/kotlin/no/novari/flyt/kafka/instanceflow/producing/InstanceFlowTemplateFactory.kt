package no.novari.flyt.kafka.instanceflow.producing

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper
import no.novari.kafka.producing.ParameterizedTemplateFactory
import org.springframework.stereotype.Service

@Service
class InstanceFlowTemplateFactory(
    private val parameterizedTemplateFactory: ParameterizedTemplateFactory,
    private val instanceFlowHeadersMapper: InstanceFlowHeadersMapper,
) {
    fun <VALUE> createTemplate(valueClass: Class<VALUE>): InstanceFlowTemplate<VALUE> =
        InstanceFlowTemplate(
            parameterizedTemplate = parameterizedTemplateFactory.createTemplate(valueClass),
            instanceFlowHeadersMapper = instanceFlowHeadersMapper,
        )
}
