package no.novari.flyt.kafka.model

public data class Error(
    val errorCode: String? = null,
    val args: Map<String, String?>? = null,
) {
    // Matches the Java v6 behaviour (no @ToString) so that potentially sensitive `args` are
    // not exposed through default logging of the data class.
    override fun toString(): String = "${javaClass.name}@${Integer.toHexString(hashCode())}"

    public class Builder internal constructor() {
        private var errorCode: String? = null
        private var args: Map<String, String?>? = null

        public fun errorCode(errorCode: String?): Builder = apply { this.errorCode = errorCode }

        public fun args(args: Map<String, String?>?): Builder = apply { this.args = args }

        public fun build(): Error = Error(errorCode = errorCode, args = args)
    }

    public companion object {
        @JvmStatic
        public fun builder(): Builder = Builder()
    }
}
