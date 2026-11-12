package no.novari.flyt.kafka.wirecompat

import java.io.File

/**
 * A record as it is stored on Kafka, in `wire-compat/fixtures/<version>/<case>.json`. All header values and the value
 * in flyt are UTF-8 text, so they are stored as strings.
 */
data class FixtureRecord(
    val case: String,
    val topic: String,
    val key: String?,
    val headers: List<FixtureHeader>,
    val value: String?,
)

data class FixtureHeader(
    val key: String,
    val value: String,
)

object WireCompatFixtures {
    /** Set by Gradle to `wire-compat/fixtures` in the root project. */
    val directory: File =
        File(
            requireNotNull(System.getProperty("wireCompat.fixtures")) { "wireCompat.fixtures is not set" },
        )

    fun file(
        version: String,
        case: String,
    ): File = directory.resolve(version).resolve("$case.json")

    /** All fixture files, by version directory. */
    fun all(): List<Pair<String, File>> =
        directory
            .listFiles(File::isDirectory)
            .orEmpty()
            .sortedBy { it.name }
            .flatMap { version ->
                version
                    .listFiles { file -> file.extension == "json" }
                    .orEmpty()
                    .sortedBy { it.name }
                    .map { version.name to it }
            }
}
