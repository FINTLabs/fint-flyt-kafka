package no.novari.flyt.kafka.headers

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class InstanceFlowHeadersTest {
    @Test
    fun `builder produces InstanceFlowHeaders when required fields are set`() {
        val instanceFlowHeaders =
            InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build()

        assertNotNull(instanceFlowHeaders)
    }

    @Test
    fun `toBuilder copies existing values and allows overriding`() {
        val original =
            InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build()

        val modified =
            original
                .toBuilder()
                .instanceId(1L)
                .build()

        val expected =
            InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .instanceId(1L)
                .build()

        assertEquals(expected, modified)
    }

    @Test
    fun `builder throws NullPointerException when sourceApplicationId is missing`() {
        assertThrows(NullPointerException::class.java) {
            InstanceFlowHeaders
                .builder()
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build()
        }
    }

    @Test
    fun `builder throws NullPointerException when correlationId is missing`() {
        assertThrows(NullPointerException::class.java) {
            InstanceFlowHeaders
                .builder()
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .build()
        }
    }
}
