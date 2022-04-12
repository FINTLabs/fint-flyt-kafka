package no.fintlabs.flyt.kafka.event;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.common.FintTemplateFactory;
import no.fintlabs.kafka.event.EventProducerFactory;
import no.fintlabs.kafka.event.topic.EventTopicMappingService;
import org.springframework.stereotype.Service;

@Service
public class FlytEventProducerFactory extends EventProducerFactory {

    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public FlytEventProducerFactory(
            FintTemplateFactory fintTemplateFactory,
            EventTopicMappingService eventTopicMappingService,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        super(fintTemplateFactory, eventTopicMappingService);
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <T> InstanceFlowEventProducer<T> createInstanceFlowProducer(Class<T> valueClass) {
        return new InstanceFlowEventProducer<>(
                createProducer(valueClass),
                instanceFlowHeadersMapper
        );
    }
}
