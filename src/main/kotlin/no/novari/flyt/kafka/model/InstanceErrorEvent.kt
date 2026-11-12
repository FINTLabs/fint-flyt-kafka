package no.novari.flyt.kafka.model

public data class InstanceErrorEvent(
    val name: InstanceErrorOrigin,
    val errors: ErrorCollection,
)
