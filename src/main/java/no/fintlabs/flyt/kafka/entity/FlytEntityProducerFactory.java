package no.fintlabs.flyt.kafka.entity;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.common.FintTemplateFactory;
import no.fintlabs.kafka.entity.EntityProducerFactory;
import no.fintlabs.kafka.entity.topic.EntityTopicMappingService;
import org.springframework.stereotype.Service;

@Service
public class FlytEntityProducerFactory extends EntityProducerFactory {

    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public FlytEntityProducerFactory(
            FintTemplateFactory fintTemplateFactory,
            EntityTopicMappingService entityTopicMappingService,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        super(fintTemplateFactory, entityTopicMappingService);
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <T> InstanceFlowEntityProducer<T> createInstanceFlowProducer(Class<T> valueClass) {
        return new InstanceFlowEntityProducer<>(
                createProducer(valueClass),
                instanceFlowHeadersMapper
        );
    }

}
