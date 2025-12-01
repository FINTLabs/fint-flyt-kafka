package no.novari.flyt.kafka.instanceflow.producing;

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper;
import no.novari.kafka.producing.ParameterizedTemplateFactory;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowTemplateFactory {

    private final ParameterizedTemplateFactory parameterizedTemplateFactory;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowTemplateFactory(
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        this.parameterizedTemplateFactory = parameterizedTemplateFactory;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <VALUE> InstanceFlowTemplate<VALUE> createTemplate(Class<VALUE> valueClass) {
        return new InstanceFlowTemplate<>(
                parameterizedTemplateFactory.createTemplate(valueClass),
                instanceFlowHeadersMapper
        );
    }
}
