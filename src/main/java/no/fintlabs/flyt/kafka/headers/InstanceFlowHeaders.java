package no.fintlabs.flyt.kafka.headers;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class InstanceFlowHeaders {

    @NonNull
    private String orgId;

    @NonNull
    private String sourceApplicationId;

    private String sourceApplicationIntegrationId;

    private String sourceApplicationInstanceId;

    @NonNull
    private String correlationId;

    private String instanceId;

    private String configurationId;

    private String archiveCaseId;

}
