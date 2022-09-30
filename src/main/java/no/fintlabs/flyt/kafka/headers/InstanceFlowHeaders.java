package no.fintlabs.flyt.kafka.headers;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class InstanceFlowHeaders {

    @NonNull
    private Long sourceApplicationId;
    private String sourceApplicationIntegrationId;
    private String sourceApplicationInstanceId;

    @NonNull
    private String correlationId;
    private Long integrationId;
    private Long instanceId;
    private Long configurationId;

    private String archiveInstanceId;

}
