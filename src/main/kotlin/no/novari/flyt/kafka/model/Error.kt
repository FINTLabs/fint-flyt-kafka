package no.novari.flyt.kafka.model

data class Error(
    val errorCode: String? = null,
    val args: Map<String, String?>? = null,
) {
    // Matches the Java v6 behaviour (no @ToString) so that potentially sensitive `args` are
    // not exposed through default logging of the data class.
    override fun toString(): String = "${javaClass.name}@${Integer.toHexString(hashCode())}"

    class Builder internal constructor() {
        private var errorCode: String? = null
        private var args: Map<String, String?>? = null

        fun errorCode(errorCode: String?) = apply { this.errorCode = errorCode }

        fun args(args: Map<String, String?>?) = apply { this.args = args }

        fun build(): Error = Error(errorCode = errorCode, args = args)
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
