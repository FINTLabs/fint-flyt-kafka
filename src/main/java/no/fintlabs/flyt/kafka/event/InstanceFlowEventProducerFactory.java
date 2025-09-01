package no.fintlabs.flyt.kafka.event;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.producing.ParameterizedTemplateFactory;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowEventProducerFactory {

    private final ParameterizedTemplateFactory parameterizedTemplateFactory;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowEventProducerFactory(ParameterizedTemplateFactory parameterizedTemplateFactory, InstanceFlowHeadersMapper instanceFlowHeadersMapper) {
        this.parameterizedTemplateFactory = parameterizedTemplateFactory;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <T> InstanceFlowEventProducer<T> createProducer(Class<T> valueClass) {
        return new InstanceFlowEventProducer<>(
                parameterizedTemplateFactory.createTemplate(valueClass),
                instanceFlowHeadersMapper
        );
    }
}
