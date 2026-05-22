package no.novari.flyt.kafka.model

data class ErrorCollection(
    val errors: Collection<Error>? = null,
) {
    constructor(vararg errors: Error) : this(errors.toList())
}
