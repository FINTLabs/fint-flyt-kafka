package no.novari.flyt.kafka.instanceflow.headers

class CouldNotWriteInstanceFlowHeadersException(
    instanceFlowHeaders: InstanceFlowHeaders,
) : RuntimeException("Could not write ${instanceFlowHeaders}to bytes")
