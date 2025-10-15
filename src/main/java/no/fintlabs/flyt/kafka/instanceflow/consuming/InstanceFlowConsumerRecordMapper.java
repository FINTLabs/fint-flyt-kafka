package no.fintlabs.flyt.kafka.instanceflow.consuming;

import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstanceFlowConsumerRecordMapper {

    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    public InstanceFlowConsumerRecordMapper(InstanceFlowHeadersMapper instanceFlowHeadersMapper) {
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

    public <T> InstanceFlowConsumerRecord<T> toFlytConsumerRecord(ConsumerRecord<String, T> consumerRecord) {
        return new InstanceFlowConsumerRecord<>(instanceFlowHeadersMapper.getInstanceFlowHeaders(consumerRecord.headers()), consumerRecord);
    }

    public <T> List<InstanceFlowConsumerRecord<T>> toFlytConsumerRecords(List<ConsumerRecord<String, T>> consumerRecords) {
        return consumerRecords.stream().map(this::toFlytConsumerRecord).toList();
    }

}
