package no.novari.flyt.kafka.instanceflow.headers

import org.apache.kafka.common.header.Header

class CouldNotReadInstanceFlowHeadersException(
    header: Header,
) : RuntimeException("Could not read instance flow headers from $header")
