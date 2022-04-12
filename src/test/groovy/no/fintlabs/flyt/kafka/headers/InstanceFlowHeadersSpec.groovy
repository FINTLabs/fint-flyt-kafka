package no.fintlabs.flyt.kafka.headers

import spock.lang.Specification

class InstanceFlowHeadersSpec extends Specification {

    def 'should create instance flow headers'() {
        when:
        def instanceFlowHeaders = InstanceFlowHeaders.builder()
                .orgId("orgId")
                .service("service")
                .sourceApplication("sourceApplication")
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId("correlationId")
                .build()
        then:
        instanceFlowHeaders
    }

    def 'should create instance flow headers from existing instance flow headers'() {
        given:
        def instanceFlowHeaders1 = InstanceFlowHeaders.builder()
                .orgId("orgId")
                .service("service")
                .sourceApplication("sourceApplication")
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId("correlationId")
                .build()

        when:
        def instanceFlowHeaders = instanceFlowHeaders1.toBuilder()
                .instanceId("instanceId")
                .build()

        then:
        instanceFlowHeaders == InstanceFlowHeaders.builder()
                .orgId("orgId")
                .service("service")
                .sourceApplication("sourceApplication")
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId("correlationId")
                .instanceId("instanceId")
                .build()
    }

    def 'should throw error if orgId is missing'() {
        when:
        InstanceFlowHeaders.builder()
                .service("service")
                .sourceApplication("sourceApplication")
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId("correlationId")
                .build()
        then:
        thrown NullPointerException
    }

    def 'should throw error if service is missing '() {
        when:
        InstanceFlowHeaders.builder()
                .orgId("orgId")
                .sourceApplication("sourceApplication")
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId("correlationId")
                .build()
        then:
        thrown NullPointerException
    }

    def 'should throw error if source application is missing '() {
        when:
        InstanceFlowHeaders.builder()
                .orgId("orgId")
                .service("service")
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId("correlationId")
                .build()
        then:
        thrown NullPointerException
    }

    def 'should throw error if source application integration id is missing '() {
        when:
        InstanceFlowHeaders.builder()
                .orgId("orgId")
                .service("service")
                .sourceApplication("sourceApplication")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId("correlationId")
                .build()
        then:
        thrown NullPointerException
    }

    def 'should throw error if source application instance id is missing '() {
        when:
        InstanceFlowHeaders.builder()
                .orgId("orgId")
                .service("service")
                .sourceApplication("sourceApplication")
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .correlationId("correlationId")
                .build()
        then:
        thrown NullPointerException
    }

    def 'should throw error if correlation id is missing '() {
        when:
        InstanceFlowHeaders.builder()
                .orgId("orgId")
                .service("service")
                .sourceApplication("sourceApplication")
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .build()
        then:
        thrown NullPointerException
    }

}
