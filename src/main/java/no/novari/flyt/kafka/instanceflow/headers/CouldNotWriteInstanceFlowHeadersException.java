package no.novari.flyt.kafka.instanceflow.headers;

public class CouldNotWriteInstanceFlowHeadersException extends RuntimeException {

    public CouldNotWriteInstanceFlowHeadersException(InstanceFlowHeaders instanceFlowHeaders) {
        super("Could not write " + instanceFlowHeaders + "to bytes");
    }

}
