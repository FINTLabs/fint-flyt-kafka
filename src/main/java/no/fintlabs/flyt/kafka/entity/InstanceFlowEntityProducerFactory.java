package no.fintlabs.flyt.kafka.entity;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.producing.ParameterizedTemplateFactory;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowEntityProducerFactory {

    private final ParameterizedTemplateFactory parameterizedTemplateFactory;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowEntityProducerFactory(
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        this.parameterizedTemplateFactory = parameterizedTemplateFactory;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <T> InstanceFlowEntityProducer<T> createProducer(Class<T> valueClass) {
        return new InstanceFlowEntityProducer<>(
                parameterizedTemplateFactory.createTemplate(valueClass),
                instanceFlowHeadersMapper
        );
    }

}
