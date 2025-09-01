package no.fintlabs.flyt.kafka.event.error;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.model.ErrorCollection;
import no.fintlabs.kafka.model.ParameterizedProducerRecord;
import no.fintlabs.kafka.producing.ParameterizedTemplate;
import no.fintlabs.kafka.producing.ParameterizedTemplateFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class InstanceFlowErrorEventProducer {

    private final ParameterizedTemplate<ErrorCollection> parameterizedTemplate;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowErrorEventProducer(
            ParameterizedTemplateFactory parameterizedTemplateFactory,
            InstanceFlowHeadersMapper instanceFlowHeadersMapper
    ) {
        this.parameterizedTemplate = parameterizedTemplateFactory.createTemplate(ErrorCollection.class);
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public CompletableFuture<SendResult<String, ErrorCollection>> send(
            InstanceFlowErrorEventProducerRecord instanceFlowErrorEventProducerRecord) {
        return parameterizedTemplate.send(
                ParameterizedProducerRecord
                        .<ErrorCollection>builder()
                        .topicNameParameters(instanceFlowErrorEventProducerRecord.getTopicNameParameters())
                        .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowErrorEventProducerRecord.getInstanceFlowHeaders()))
                        .value(instanceFlowErrorEventProducerRecord.getErrorCollection())
                        .build()
        );
    }

}
