package no.fintlabs.flyt.kafka;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;


public abstract class InstanceFlowErrorHandler extends DefaultErrorHandler {

    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    protected InstanceFlowErrorHandler(InstanceFlowHeadersMapper instanceFlowHeadersMapper) {
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    @Override
    public void handleRecord(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, MessageListenerContainer container) {
        handleInstanceFlowRecord(
                thrownException,
                instanceFlowHeadersMapper.getInstanceFlowHeaders(record.headers()),
                record
        );
    }

    public abstract void handleInstanceFlowRecord(
            Exception thrownException,
            InstanceFlowHeaders instanceFlowHeaders,
            ConsumerRecord<?, ?> record
    );

}
