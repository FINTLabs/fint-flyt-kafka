package no.fintlabs.flyt.kafka.headers;

public class NoInstanceFlowHeadersException extends RuntimeException {

    public NoInstanceFlowHeadersException() {
        super("No instance flow headers");
    }
}
