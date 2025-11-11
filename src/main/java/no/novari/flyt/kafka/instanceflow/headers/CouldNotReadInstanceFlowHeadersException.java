package no.novari.flyt.kafka.instanceflow.headers;

import org.apache.kafka.common.header.Header;

public class CouldNotReadInstanceFlowHeadersException extends RuntimeException {

    public CouldNotReadInstanceFlowHeadersException(Header header) {
        super("Could not read instance flow headers from " + header);
    }

}
