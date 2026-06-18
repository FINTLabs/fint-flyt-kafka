package no.novari.flyt.kafka.model

data class ErrorCollection(
    val errors: Collection<Error>? = null,
) {
    constructor(vararg errors: Error) : this(errors.toList())

    // Matches the Java v6 behaviour (no @ToString) so that potentially sensitive errors
    // are not exposed through default logging of the data class.
    override fun toString(): String = "${javaClass.name}@${Integer.toHexString(hashCode())}"
}
