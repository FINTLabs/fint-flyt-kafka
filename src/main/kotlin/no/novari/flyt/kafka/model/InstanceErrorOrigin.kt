package no.novari.flyt.kafka.model

enum class InstanceErrorOrigin {
    RECEIVAL,
    REGISTRATION,
    RETRY_REQUEST,
    MAPPING,
    DISPATCHING,
}
