package no.novari.flyt.kafka.instanceflow.headers;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Jacksonized
@ToString
@Builder(toBuilder = true)
public class InstanceFlowHeaders {

    @NonNull
    private Long sourceApplicationId;
    private String sourceApplicationIntegrationId;
    private String sourceApplicationInstanceId;

    private List<UUID> fileIds;

    @NonNull
    private UUID correlationId;
    private Long integrationId;
    private Long instanceId;
    private Long configurationId;

    private String archiveInstanceId;

}
