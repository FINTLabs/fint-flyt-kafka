package no.novari.flyt.kafka.wirecompat

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.model.Error
import no.novari.flyt.kafka.model.ErrorCollection
import no.novari.flyt.kafka.model.InstanceErrorEvent
import no.novari.flyt.kafka.model.InstanceErrorOrigin
import java.time.Instant
import java.util.Date
import java.util.UUID

/**
 * Test cases for the wire compatibility tests (FINT-130): one record per step in the life cycle of an instance in flyt.
 * See `fint-130-livssyklus-flyt.md` in the docs repo.
 *
 * Shared by the root project and `compat-v7`, so it only uses classes that exist with the same name in fint-flyt-kafka
 * 7 and in the version built on fint-kafka-starter.
 */
object WireCompatCases {
    const val ORG_ID = "afk-no"
    const val DOMAIN_CONTEXT = "flyt"
    const val ORIGIN_APPLICATION_ID_HEADER = "origin.application.id"
    const val ACTOR_HEADER = "flyt.actor"

    private val receivedCorrelationId = UUID.fromString("0f6c2a4e-6a1b-4c8e-9a51-3d2f7b8e1c01")
    private val retryCorrelationId = UUID.fromString("5b9e4d2a-1c3f-4e7a-8b6d-9f0a2c4e6b02")
    private val fileIds =
        listOf(
            UUID.fromString("a1d5e7f9-2b4c-4d6e-8f0a-1b3c5d7e9f03"),
            UUID.fromString("b2e6f8a0-3c5d-4e7f-9a1b-2c4d6e8f0a04"),
        )

    private val received =
        InstanceFlowHeaders(
            sourceApplicationId = 2,
            correlationId = receivedCorrelationId,
            sourceApplicationIntegrationId = "skoleskyss-soknad",
            sourceApplicationInstanceId = "SKY-2026-004711",
            fileIds = fileIds,
            integrationId = 34,
        )
    private val registered = received.copy(instanceId = 567)
    private val retried = registered.copy(correlationId = retryCorrelationId)
    private val mapped = registered.copy(configurationId = 89)
    private val dispatched = mapped.copy(archiveInstanceId = "2026/1234-5")

    /** Headers known when the gateway fails before it has found the integration. */
    private val receivalFailed = received.copy(fileIds = emptyList(), integrationId = null)

    private val gatewayInstance =
        GatewayInstanceObject(
            valuePerKey =
                mapOf(
                    "tittel" to "Søknad om skoleskyss",
                    "skolenavn" to "Åsen videregående skole",
                    "elevnummer" to "4711",
                ),
            objectCollectionPerKey =
                mapOf(
                    "vedlegg" to
                        listOf(
                            GatewayInstanceObject(mapOf("tittel" to "Vedtak", "fil" to fileIds[0].toString())),
                            GatewayInstanceObject(mapOf("tittel" to "Kart", "fil" to fileIds[1].toString())),
                        ),
                ),
        )

    private val registeredInstance =
        InstanceObjectDto(
            id = 567,
            valuePerKey = gatewayInstance.valuePerKey,
            objectCollectionPerKey =
                mapOf(
                    "vedlegg" to
                        listOf(
                            InstanceObjectDto(id = 568, valuePerKey = mapOf("tittel" to "Vedtak")),
                            InstanceObjectDto(id = 569, valuePerKey = mapOf("tittel" to "Kart")),
                        ),
                ),
            createdAt = Date.from(Instant.parse("2026-11-12T10:15:30.123Z")),
        )

    /** What mapping-service reads from [registeredInstance]. */
    private val registeredInstanceForMapping =
        MappingInstanceObject(
            valuePerKey = gatewayInstance.valuePerKey,
            objectCollectionPerKey =
                mapOf(
                    "vedlegg" to
                        listOf(
                            MappingInstanceObject(mapOf("tittel" to "Vedtak")),
                            MappingInstanceObject(mapOf("tittel" to "Kart")),
                        ),
                ),
        )

    /** mapping-service sends the mapped instance as a map. */
    private val mappedInstance: Map<String, Any?> =
        mapOf(
            "type" to "NEW",
            "caseId" to null,
            "newCase" to
                mapOf(
                    "tittel" to "Skoleskyss 2026/2027",
                    "offentligTittel" to "Skoleskyss",
                    "saksmappetype" to "Sak",
                    "administrativEnhet" to "215",
                ),
            "journalpost" to
                listOf(
                    mapOf(
                        "tittel" to "Søknad om skoleskyss",
                        "journalposttype" to "I",
                        "dokumentbeskrivelse" to
                            listOf(
                                mapOf(
                                    "tittel" to "Vedtak",
                                    "dokumentstatus" to "F",
                                    "dokumentObjekt" to
                                        listOf(
                                            mapOf(
                                                "fileId" to fileIds[0].toString(),
                                                "format" to "PDF",
                                                "variantformat" to "A",
                                            ),
                                        ),
                                ),
                            ),
                    ),
                ),
        )

    private val archiveInstance =
        ArchiveInstance(
            type = CaseDispatchType.NEW,
            newCase =
                Sak(
                    tittel = "Skoleskyss 2026/2027",
                    offentligTittel = "Skoleskyss",
                    saksmappetype = "Sak",
                    administrativEnhet = "215",
                ),
            journalpost =
                listOf(
                    Journalpost(
                        tittel = "Søknad om skoleskyss",
                        journalposttype = "I",
                        dokumentbeskrivelse =
                            listOf(
                                Dokumentbeskrivelse(
                                    tittel = "Vedtak",
                                    dokumentstatus = "F",
                                    dokumentObjekt = listOf(DokumentObjekt(fileIds[0], "PDF", "A")),
                                ),
                            ),
                    ),
                ),
        )

    val all: List<WireCompatCase> =
        listOf(
            WireCompatCase(
                name = "instance-received",
                topicType = TopicType.EVENT,
                eventName = "instance-received",
                instanceFlowHeaders = received,
                value = gatewayInstance,
                consumers =
                    listOf(
                        Consumer(
                            InstanceObjectDto::class.java,
                            Expect.Equal(
                                InstanceObjectDto(
                                    valuePerKey = gatewayInstance.valuePerKey,
                                    objectCollectionPerKey =
                                        mapOf(
                                            "vedlegg" to
                                                gatewayInstance.objectCollectionPerKey
                                                    .getValue("vedlegg")
                                                    .map { InstanceObjectDto(valuePerKey = it.valuePerKey) },
                                        ),
                                ),
                            ),
                        ),
                        Consumer(Any::class.java, Expect.Readable),
                    ),
            ),
            WireCompatCase(
                name = "instance-registered",
                topicType = TopicType.EVENT,
                eventName = "instance-registered",
                instanceFlowHeaders = registered,
                value = registeredInstance,
                consumers =
                    listOf(
                        Consumer(MappingInstanceObject::class.java, Expect.Equal(registeredInstanceForMapping)),
                        Consumer(
                            InstanceObjectDto::class.java,
                            // id is READ_ONLY, so it is written but not read.
                            Expect.Equal(
                                registeredInstance.copy(
                                    id = null,
                                    objectCollectionPerKey =
                                        mapOf(
                                            "vedlegg" to
                                                listOf(
                                                    InstanceObjectDto(valuePerKey = mapOf("tittel" to "Vedtak")),
                                                    InstanceObjectDto(valuePerKey = mapOf("tittel" to "Kart")),
                                                ),
                                        ),
                                ),
                            ),
                        ),
                        Consumer(Any::class.java, Expect.Readable),
                    ),
            ),
            WireCompatCase(
                name = "instance-requested-for-retry",
                topicType = TopicType.EVENT,
                eventName = "instance-requested-for-retry",
                instanceFlowHeaders = retried,
                additionalHeaders =
                    mapOf(ACTOR_HEADER to """{"type":"USER","oid":"2ee6f95e-44c3-11ed-b878-0242ac120002"}"""),
                value = registeredInstance,
                consumers =
                    listOf(
                        Consumer(MappingInstanceObject::class.java, Expect.Equal(registeredInstanceForMapping)),
                        Consumer(Any::class.java, Expect.Readable),
                    ),
            ),
            WireCompatCase(
                name = "instance-mapped",
                topicType = TopicType.EVENT,
                eventName = "instance-mapped",
                instanceFlowHeaders = mapped,
                value = mappedInstance,
                consumers =
                    listOf(
                        Consumer(ArchiveInstance::class.java, Expect.Equal(archiveInstance)),
                        Consumer(Any::class.java, Expect.Readable),
                    ),
            ),
            WireCompatCase(
                name = "instance-dispatched",
                topicType = TopicType.EVENT,
                eventName = "instance-dispatched",
                instanceFlowHeaders = dispatched,
                value = null,
                consumers =
                    listOf(
                        Consumer(InstanceObjectDto::class.java, Expect.Equal(null)),
                        Consumer(Any::class.java, Expect.Equal(null)),
                    ),
            ),
            WireCompatCase(
                name = "instance-deleted",
                topicType = TopicType.EVENT,
                eventName = "instance-deleted",
                instanceFlowHeaders = dispatched,
                value = null,
                consumers = listOf(Consumer(Any::class.java, Expect.Equal(null))),
            ),
            WireCompatCase(
                name = "instance-error-receival",
                topicType = TopicType.ERROR_EVENT,
                eventName = "instance-error",
                instanceFlowHeaders = receivalFailed,
                value =
                    InstanceErrorEvent(
                        name = InstanceErrorOrigin.RECEIVAL,
                        errors =
                            ErrorCollection(
                                Error(
                                    errorCode = "NO_INTEGRATION_FOUND_ERROR",
                                    args =
                                        mapOf(
                                            "sourceApplicationId" to "2",
                                            "sourceApplicationIntegrationId" to "skoleskyss-soknad",
                                        ),
                                ),
                            ),
                    ),
                consumers = listOf(Consumer(InstanceErrorEvent::class.java, Expect.EqualToValue)),
            ),
            WireCompatCase(
                name = "instance-error-dispatching",
                topicType = TopicType.ERROR_EVENT,
                eventName = "instance-error",
                instanceFlowHeaders = mapped,
                value =
                    InstanceErrorEvent(
                        name = InstanceErrorOrigin.DISPATCHING,
                        errors =
                            ErrorCollection(
                                Error(errorCode = "INSTANCE_DISPATCH_DECLINED", args = mapOf("message" to null)),
                                Error(errorCode = "GENERAL_SYSTEM_ERROR"),
                            ),
                    ),
                consumers = listOf(Consumer(InstanceErrorEvent::class.java, Expect.EqualToValue)),
            ),
            WireCompatCase(
                name = "instance-mapping-error-legacy",
                topicType = TopicType.ERROR_EVENT,
                eventName = "instance-mapping-error",
                instanceFlowHeaders = registered,
                value =
                    ErrorCollection(
                        Error(errorCode = "MISSING_VALUE", args = mapOf("key" to "skolenavn")),
                    ),
                consumers = listOf(Consumer(ErrorCollection::class.java, Expect.EqualToValue)),
            ),
        )

    fun byName(name: String): WireCompatCase = all.single { it.name == name }
}

enum class TopicType {
    EVENT,
    ERROR_EVENT,
}

data class WireCompatCase(
    val name: String,
    val topicType: TopicType,
    val eventName: String,
    val instanceFlowHeaders: InstanceFlowHeaders,
    val additionalHeaders: Map<String, String> = emptyMap(),
    val value: Any?,
    /** The classes the flyt apps read this record with. */
    val consumers: List<Consumer>,
) {
    val topic: String =
        listOfNotNull(
            WireCompatCases.ORG_ID,
            WireCompatCases.DOMAIN_CONTEXT,
            "event",
            "error".takeIf { topicType == TopicType.ERROR_EVENT },
            eventName,
        ).joinToString(".")

    fun expected(consumer: Consumer): Any? =
        when (val expect = consumer.expect) {
            is Expect.Equal -> {
                expect.value
            }

            Expect.EqualToValue -> {
                value
            }

            Expect.Readable -> {
                throw IllegalArgumentException("$name: ${consumer.type.simpleName} has no expected value")
            }
        }
}

data class Consumer(
    val type: Class<*>,
    val expect: Expect,
)

sealed interface Expect {
    /** The consumer reads [value]. */
    data class Equal(
        val value: Any?,
    ) : Expect

    /** The consumer reads the value the case produced. */
    data object EqualToValue : Expect

    /** The consumer reads the record without error. Used for `Any`, where the apps do not use the value. */
    data object Readable : Expect
}
