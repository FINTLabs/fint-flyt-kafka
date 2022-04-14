package no.fintlabs.flyt.kafka.entity;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowEntityProducerFactory {

    private final EntityProducerFactory entityProducerFactory;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowEntityProducerFactory(
            EntityProducerFactory entityProducerFactory,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        this.entityProducerFactory = entityProducerFactory;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <T> InstanceFlowEntityProducer<T> createInstanceFlowProducer(Class<T> valueClass) {
        return new InstanceFlowEntityProducer<>(
                entityProducerFactory.createProducer(valueClass),
                instanceFlowHeadersMapper
        );
    }

}
