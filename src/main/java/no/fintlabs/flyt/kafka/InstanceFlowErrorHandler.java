package no.fintlabs.flyt.kafka;

import lombok.NonNull;
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
    public boolean handleOne(
            @NonNull Exception thrownException,
            @NonNull ConsumerRecord<?, ?> record,
            @NonNull Consumer<?, ?> consumer,
            @NonNull MessageListenerContainer container
    ) {
        this.handleInstanceFlowRecord(
                thrownException,
                this.instanceFlowHeadersMapper.getInstanceFlowHeaders(record.headers()),
                record
        );
        return super.handleOne(thrownException, record, consumer, container);
    }

    public abstract void handleInstanceFlowRecord(
            Throwable cause,
            InstanceFlowHeaders instanceFlowHeaders,
            ConsumerRecord<?, ?> consumerRecord
    );

}
