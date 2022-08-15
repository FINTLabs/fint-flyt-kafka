package no.fintlabs.flyt.kafka.headers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class InstanceFlowHeadersMapper {

    private static final String INSTANCE_FLOW_HEADERS_KEY = "flyt.instance-flow-headers";

    private final ObjectMapper objectMapper;

    public InstanceFlowHeadersMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Header toHeader(InstanceFlowHeaders instanceFlowHeaders) {
        if (instanceFlowHeaders == null) {
            throw new NoInstanceFlowHeadersException();
        }
        try {
            return new RecordHeader(INSTANCE_FLOW_HEADERS_KEY, objectMapper.writeValueAsBytes(instanceFlowHeaders));
        } catch (JsonProcessingException e) {
            throw new CouldNotWriteInstanceFlowHeadersException(instanceFlowHeaders);
        }
    }

    public Headers toHeaders(InstanceFlowHeaders instanceFlowHeaders) {
        return new RecordHeaders().add(toHeader(instanceFlowHeaders));
    }

    public InstanceFlowHeaders getInstanceFlowHeaders(Headers headers) {
        Header header = headers.lastHeader(INSTANCE_FLOW_HEADERS_KEY);
        if (header == null) {
            throw new NoInstanceFlowHeadersException();
        }
        return toInstanceFlowHeaders(header);
    }

    public InstanceFlowHeaders toInstanceFlowHeaders(Header header) {
        try {
            return objectMapper.readValue(header.value(), InstanceFlowHeaders.class);
        } catch (IOException e) {
            throw new CouldNotReadInstanceFlowHeadersException(header);
        }
    }

}
