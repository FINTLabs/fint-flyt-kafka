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
    private String service;

    @NonNull
    private String sourceApplication;

    @NonNull
    private String sourceApplicationIntegrationId;

    @NonNull
    private String sourceApplicationInstanceId;

    @NonNull
    private String correlationId;

// TODO: 08/04/2022 Remove integrationId and instanceId? If we keep these, we need a service for storing integration ids
//    private String integrationId;
    private String instanceId;

    private String configurationId;

    private String caseId;

    private String dispatchId;

}

