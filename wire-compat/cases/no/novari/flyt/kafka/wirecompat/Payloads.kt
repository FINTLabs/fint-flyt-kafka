package no.novari.flyt.kafka.wirecompat

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date
import java.util.UUID

// Copies of the payload classes in the flyt apps, reduced to what shapes the JSON. As in flyt, a record is often read
// with another class than the one that wrote it.

/** `InstanceObject` in fint-flyt-gateway-starter and fint-flyt-web-instance-gateway. */
data class GatewayInstanceObject(
    val valuePerKey: Map<String, String> = emptyMap(),
    val objectCollectionPerKey: Map<String, Collection<GatewayInstanceObject>> = emptyMap(),
)

/** `InstanceObjectDto` in fint-flyt-instance-service and fint-flyt-history-service. */
data class InstanceObjectDto(
    @field:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val id: Long? = null,
    val valuePerKey: Map<String, String> = emptyMap(),
    val objectCollectionPerKey: Map<String, Collection<InstanceObjectDto>> = emptyMap(),
    val createdAt: Date? = null,
)

/** `InstanceObject` in fint-flyt-mapping-service. */
data class MappingInstanceObject(
    val valuePerKey: Map<String, String?> = emptyMap(),
    val objectCollectionPerKey: Map<String, Collection<MappingInstanceObject>> = emptyMap(),
)

/** `ArchiveInstance` in fint-flyt-archive-gateway, reduced. */
data class ArchiveInstance(
    val type: CaseDispatchType? = null,
    val caseId: String? = null,
    val newCase: Sak? = null,
    val journalpost: List<Journalpost>? = null,
)

enum class CaseDispatchType {
    NEW,
    BY_ID,
    BY_SEARCH_OR_NEW,
}

data class Sak(
    val tittel: String? = null,
    val offentligTittel: String? = null,
    val saksmappetype: String? = null,
    val administrativEnhet: String? = null,
)

data class Journalpost(
    val tittel: String? = null,
    val journalposttype: String? = null,
    val dokumentbeskrivelse: List<Dokumentbeskrivelse>? = null,
)

data class Dokumentbeskrivelse(
    val tittel: String? = null,
    val dokumentstatus: String? = null,
    val dokumentObjekt: List<DokumentObjekt>? = null,
)

data class DokumentObjekt(
    val fileId: UUID? = null,
    val format: String? = null,
    val variantformat: String? = null,
)
