package no.fintlabs.flyt.kafka;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
public class InstanceFlowConsumerRecordMapper {

    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowConsumerRecordMapper(InstanceFlowHeadersMapper instanceFlowHeadersMapper) {
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <T> InstanceFlowConsumerRecord<T> toFlytConsumerRecord(ConsumerRecord<String, T> consumerRecord) {
        return new InstanceFlowConsumerRecord<>(instanceFlowHeadersMapper.getInstanceFlowHeaders(consumerRecord.headers()), consumerRecord);
    }

}
