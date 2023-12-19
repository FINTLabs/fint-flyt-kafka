package no.fintlabs.flyt.kafka.headers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

class InstanceFlowHeadersTest {

    @Test
    void shouldCreateInstanceFlowHeaders() {
        InstanceFlowHeaders instanceFlowHeaders = InstanceFlowHeaders.builder()
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build();

        assertNotNull(instanceFlowHeaders);
    }

    @Test
    void shouldCreateInstanceFlowHeadersFromExistingInstanceFlowHeaders() {
        InstanceFlowHeaders instanceFlowHeaders1 = InstanceFlowHeaders.builder()
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build();

        InstanceFlowHeaders instanceFlowHeaders = instanceFlowHeaders1.toBuilder()
                .instanceId(1L)
                .build();

        InstanceFlowHeaders expected = InstanceFlowHeaders.builder()
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .instanceId(1L)
                .build();

        assertEquals(expected, instanceFlowHeaders);
    }

    @Test
    void shouldThrowErrorIfSourceApplicationIdIsMissing() {
        assertThrows(NullPointerException.class, () ->
                InstanceFlowHeaders.builder()
                        .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                        .sourceApplicationInstanceId("sourceApplicationInstanceId")
                        .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                        .build()
        );
    }

    @Test
    void shouldThrowErrorIfCorrelationIdIsMissing() {
        assertThrows(NullPointerException.class, () ->
                InstanceFlowHeaders.builder()
                        .sourceApplicationId(1L)
                        .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                        .sourceApplicationInstanceId("sourceApplicationInstanceId")
                        .build()
        );
    }
}
