package no.novari.flyt.kafka.instanceflow.headers

import java.util.UUID

public data class InstanceFlowHeaders(
    val sourceApplicationId: Long,
    val correlationId: UUID,
    val sourceApplicationIntegrationId: String? = null,
    val sourceApplicationInstanceId: String? = null,
    val fileIds: List<UUID>? = null,
    val integrationId: Long? = null,
    val instanceId: Long? = null,
    val configurationId: Long? = null,
    val archiveInstanceId: String? = null,
)
