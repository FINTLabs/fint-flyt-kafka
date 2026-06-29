package no.novari.flyt.kafka.model

data class InstanceErrorEvent(
    val name: InstanceErrorOrigin,
    val errors: ErrorCollection,
)
