package no.fintlabs.flyt.kafka.event;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.event.EventProducerFactory;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowEventProducerFactory {

    private final EventProducerFactory eventProducerFactory;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowEventProducerFactory(EventProducerFactory eventProducerFactory, InstanceFlowHeadersMapper instanceFlowHeadersMapper) {
        this.eventProducerFactory = eventProducerFactory;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <T> InstanceFlowEventProducer<T> createInstanceFlowProducer(Class<T> valueClass) {
        return new InstanceFlowEventProducer<>(
                eventProducerFactory.createProducer(valueClass),
                instanceFlowHeadersMapper
        );
    }
}
