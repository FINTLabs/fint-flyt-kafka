package no.fintlabs.flyt.kafka.event.error;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.common.FintTemplateFactory;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.ErrorEventProducer;
import no.fintlabs.kafka.event.error.ErrorEventProducerRecord;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicMappingService;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class FlytErrorEventProducer extends ErrorEventProducer {

    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public FlytErrorEventProducer(
            FintTemplateFactory fintTemplateFactory,
            ErrorEventTopicMappingService errorEventTopicMappingService,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        super(fintTemplateFactory, errorEventTopicMappingService);
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public ListenableFuture<SendResult<String, ErrorCollection>> send(InstanceFlowErrorEventProducerRecord instanceFlowErrorEventProducerRecord) {
        return send(
                ErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceFlowErrorEventProducerRecord.getTopicNameParameters())
                        .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowErrorEventProducerRecord.getInstanceFlowHeaders()))
                        .errorCollection(instanceFlowErrorEventProducerRecord.getErrorCollection())
                        .build()
        );
    }

}
