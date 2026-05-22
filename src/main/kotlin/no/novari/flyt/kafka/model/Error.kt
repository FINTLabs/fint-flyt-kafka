package no.novari.flyt.kafka.model

data class Error(
    val errorCode: String? = null,
    val args: Map<String, String>? = null,
) {
    class Builder internal constructor() {
        private var errorCode: String? = null
        private var args: Map<String, String>? = null

        fun errorCode(errorCode: String?) = apply { this.errorCode = errorCode }

        fun args(args: Map<String, String>?) = apply { this.args = args }

        fun build(): Error = Error(errorCode = errorCode, args = args)
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
