package no.novari.flyt.kafka.instanceflow.headers

import java.util.UUID

data class InstanceFlowHeaders(
    val sourceApplicationId: Long,
    val correlationId: UUID,
    val sourceApplicationIntegrationId: String? = null,
    val sourceApplicationInstanceId: String? = null,
    val fileIds: List<UUID>? = null,
    val integrationId: Long? = null,
    val instanceId: Long? = null,
    val configurationId: Long? = null,
    val archiveInstanceId: String? = null,
) {
    fun toBuilder(): Builder =
        Builder()
            .sourceApplicationId(sourceApplicationId)
            .correlationId(correlationId)
            .sourceApplicationIntegrationId(sourceApplicationIntegrationId)
            .sourceApplicationInstanceId(sourceApplicationInstanceId)
            .fileIds(fileIds)
            .integrationId(integrationId)
            .instanceId(instanceId)
            .configurationId(configurationId)
            .archiveInstanceId(archiveInstanceId)

    class Builder internal constructor() {
        private var sourceApplicationId: Long? = null
        private var correlationId: UUID? = null
        private var sourceApplicationIntegrationId: String? = null
        private var sourceApplicationInstanceId: String? = null
        private var fileIds: List<UUID>? = null
        private var integrationId: Long? = null
        private var instanceId: Long? = null
        private var configurationId: Long? = null
        private var archiveInstanceId: String? = null

        fun sourceApplicationId(sourceApplicationId: Long?) = apply { this.sourceApplicationId = sourceApplicationId }

        fun correlationId(correlationId: UUID?) = apply { this.correlationId = correlationId }

        fun sourceApplicationIntegrationId(value: String?) = apply { this.sourceApplicationIntegrationId = value }

        fun sourceApplicationInstanceId(value: String?) = apply { this.sourceApplicationInstanceId = value }

        fun fileIds(fileIds: List<UUID>?) = apply { this.fileIds = fileIds }

        fun integrationId(integrationId: Long?) = apply { this.integrationId = integrationId }

        fun instanceId(instanceId: Long?) = apply { this.instanceId = instanceId }

        fun configurationId(configurationId: Long?) = apply { this.configurationId = configurationId }

        fun archiveInstanceId(archiveInstanceId: String?) = apply { this.archiveInstanceId = archiveInstanceId }

        fun build(): InstanceFlowHeaders =
            InstanceFlowHeaders(
                sourceApplicationId =
                    sourceApplicationId
                        ?: throw NullPointerException("sourceApplicationId is marked non-null but is null"),
                correlationId =
                    correlationId
                        ?: throw NullPointerException("correlationId is marked non-null but is null"),
                sourceApplicationIntegrationId = sourceApplicationIntegrationId,
                sourceApplicationInstanceId = sourceApplicationInstanceId,
                fileIds = fileIds,
                integrationId = integrationId,
                instanceId = instanceId,
                configurationId = configurationId,
                archiveInstanceId = archiveInstanceId,
            )
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
