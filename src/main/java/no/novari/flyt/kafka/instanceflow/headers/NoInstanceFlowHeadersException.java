package no.novari.flyt.kafka.instanceflow.headers;

public class NoInstanceFlowHeadersException extends RuntimeException {

    public NoInstanceFlowHeadersException() {
        super("No instance flow headers");
    }
}
