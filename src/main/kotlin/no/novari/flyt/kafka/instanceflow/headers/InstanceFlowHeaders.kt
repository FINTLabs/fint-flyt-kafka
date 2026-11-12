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
) {
    public fun toBuilder(): Builder =
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

    public class Builder internal constructor() {
        private var sourceApplicationId: Long? = null
        private var correlationId: UUID? = null
        private var sourceApplicationIntegrationId: String? = null
        private var sourceApplicationInstanceId: String? = null
        private var fileIds: List<UUID>? = null
        private var integrationId: Long? = null
        private var instanceId: Long? = null
        private var configurationId: Long? = null
        private var archiveInstanceId: String? = null

        public fun sourceApplicationId(sourceApplicationId: Long?): Builder =
            apply {
                this.sourceApplicationId =
                    sourceApplicationId
            }

        public fun correlationId(correlationId: UUID?): Builder = apply { this.correlationId = correlationId }

        public fun sourceApplicationIntegrationId(value: String?): Builder =
            apply {
                this.sourceApplicationIntegrationId =
                    value
            }

        public fun sourceApplicationInstanceId(value: String?): Builder =
            apply {
                this.sourceApplicationInstanceId =
                    value
            }

        public fun fileIds(fileIds: List<UUID>?): Builder = apply { this.fileIds = fileIds }

        public fun integrationId(integrationId: Long?): Builder = apply { this.integrationId = integrationId }

        public fun instanceId(instanceId: Long?): Builder = apply { this.instanceId = instanceId }

        public fun configurationId(configurationId: Long?): Builder = apply { this.configurationId = configurationId }

        public fun archiveInstanceId(archiveInstanceId: String?): Builder =
            apply {
                this.archiveInstanceId =
                    archiveInstanceId
            }

        public fun build(): InstanceFlowHeaders =
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

    public companion object {
        @JvmStatic
        public fun builder(): Builder = Builder()
    }
}
