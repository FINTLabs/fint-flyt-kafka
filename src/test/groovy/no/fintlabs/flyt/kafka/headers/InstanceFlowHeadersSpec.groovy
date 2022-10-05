package no.fintlabs.flyt.kafka.headers

import spock.lang.Specification

class InstanceFlowHeadersSpec extends Specification {

    def 'should create instance flow headers'() {
        when:
        def instanceFlowHeaders = InstanceFlowHeaders.builder()
                .sourceApplicationId(1)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build()
        then:
        instanceFlowHeaders
    }

    def 'should create instance flow headers from existing instance flow headers'() {
        given:
        def instanceFlowHeaders1 = InstanceFlowHeaders.builder()
                .sourceApplicationId(1)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build()

        when:
        def instanceFlowHeaders = instanceFlowHeaders1.toBuilder()
                .instanceId(1)
                .build()

        then:
        instanceFlowHeaders == InstanceFlowHeaders.builder()
                .sourceApplicationId(1)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .instanceId(1)
                .build()
    }

    def 'should throw error if source application id is missing '() {
        when:
        InstanceFlowHeaders.builder()
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build()
        then:
        thrown NullPointerException
    }

    def 'should throw error if correlation id is missing '() {
        when:
        InstanceFlowHeaders.builder()
                .sourceApplicationId(1)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .build()
        then:
        thrown NullPointerException
    }

}
