package no.fintlabs.flyt.kafka.event.error;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.ErrorEventProducer;
import no.fintlabs.kafka.event.error.ErrorEventProducerRecord;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class InstanceFlowErrorEventProducer {

    private final ErrorEventProducer errorEventProducer;
    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowErrorEventProducer(ErrorEventProducer errorEventProducer, InstanceFlowHeadersMapper instanceFlowHeadersMapper) {
        this.errorEventProducer = errorEventProducer;
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }


    public ListenableFuture<SendResult<String, ErrorCollection>> send(InstanceFlowErrorEventProducerRecord instanceFlowErrorEventProducerRecord) {
        return errorEventProducer.send(
                ErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(instanceFlowErrorEventProducerRecord.getTopicNameParameters())
                        .headers(instanceFlowHeadersMapper.toHeaders(instanceFlowErrorEventProducerRecord.getInstanceFlowHeaders()))
                        .errorCollection(instanceFlowErrorEventProducerRecord.getErrorCollection())
                        .build()
        );
    }

}
