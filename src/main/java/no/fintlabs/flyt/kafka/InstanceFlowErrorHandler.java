package no.fintlabs.flyt.kafka;

import no.fintlabs.flyt.kafka.headers.InstanceFlowHeadersMapper;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;

@Service
public abstract class InstanceFlowErrorHandler extends DefaultErrorHandler {

    private final InstanceFlowHeadersMapper instanceFlowHeadersMapper;

    protected InstanceFlowErrorHandler(InstanceFlowHeadersMapper instanceFlowHeadersMapper) {
        this.instanceFlowHeadersMapper = instanceFlowHeadersMapper;
    }

}
