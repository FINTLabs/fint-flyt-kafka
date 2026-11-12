package no.novari.flyt.kafka.model

public data class Error(
    val errorCode: String? = null,
    val args: Map<String, String?>? = null,
) {
    // Matches the Java v6 behaviour (no @ToString) so that potentially sensitive `args` are
    // not exposed through default logging of the data class.
    override fun toString(): String = "${javaClass.name}@${Integer.toHexString(hashCode())}"
}
