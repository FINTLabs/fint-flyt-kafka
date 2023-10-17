package no.fintlabs.flyt.kafka

import no.fintlabs.flyt.kafka.entity.InstanceFlowEntityConsumerFactoryService
import no.fintlabs.flyt.kafka.entity.InstanceFlowEntityProducerFactory
import no.fintlabs.flyt.kafka.entity.InstanceFlowEntityProducerRecord
import no.fintlabs.flyt.kafka.event.InstanceFlowEventConsumerFactoryService
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerFactory
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerRecord
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventConsumerFactoryService
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducer
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducerRecord
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowReplyProducerRecord
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowRequestConsumerFactoryService
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowRequestProducerFactory
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowRequestProducerRecord
import no.fintlabs.kafka.common.ListenerBeanRegistrationService
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters
import no.fintlabs.kafka.event.error.Error
import no.fintlabs.kafka.event.error.ErrorCollection
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters
import no.fintlabs.kafka.event.topic.EventTopicNameParameters
import no.fintlabs.kafka.requestreply.RequestProducerConfiguration
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
class InstanceFlowProducerConsumerIntegrationSpec extends Specification {

    @Autowired
    InstanceFlowEventProducerFactory eventProducerFactory
    @Autowired
    InstanceFlowEventConsumerFactoryService eventConsumerFactory

    @Autowired
    InstanceFlowErrorEventProducer errorEventProducer
    @Autowired
    InstanceFlowErrorEventConsumerFactoryService errorEventConsumerFactory

    @Autowired
    InstanceFlowEntityProducerFactory entityProducerFactory
    @Autowired
    InstanceFlowEntityConsumerFactoryService entityConsumerFactory

    @Autowired
    InstanceFlowRequestProducerFactory requestProducerFactory
    @Autowired
    InstanceFlowRequestConsumerFactoryService requestConsumerFactory

    @Autowired
    ListenerBeanRegistrationService fintListenerBeanRegistrationService

    private static class TestObject {
        private Integer integer
        private String string

        Integer getInteger() {
            return integer
        }

        void setInteger(Integer integer) {
            this.integer = integer
        }

        String getString() {
            return string
        }

        void setString(String string) {
            this.string = string
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            TestObject that = (TestObject) o

            if (integer != that.integer) return false
            if (string != that.string) return false

            return true
        }

        int hashCode() {
            int result
            result = (integer != null ? integer.hashCode() : 0)
            result = 31 * result + (string != null ? string.hashCode() : 0)
            return result
        }
    }

    def 'event'() {
        given:
        CountDownLatch eventCDL = new CountDownLatch(1)
        ArrayList<InstanceFlowConsumerRecord<TestObject>> consumedEvents = new ArrayList<>()
        def eventProducer = eventProducerFactory.createProducer(TestObject.class)
        def eventConsumer = eventConsumerFactory.createRecordFactory(
                TestObject.class,
                (consumerRecord) -> {
                    consumedEvents.add(consumerRecord)
                    eventCDL.countDown()
                }
        ).createContainer(EventTopicNameParameters.builder().eventName("event").build())
        fintListenerBeanRegistrationService.registerBean(eventConsumer)

        when:
        TestObject testObject = new TestObject()
        testObject.setInteger(2)
        testObject.setString("testObjectString")
        eventProducer.send(
                InstanceFlowEventProducerRecord.builder()
                        .topicNameParameters(EventTopicNameParameters.builder()
                                .eventName("event")
                                .build())
                        .instanceFlowHeaders(createInstanceFlowHeaders())
                        .value(testObject)
                        .build()
        )

        eventCDL.await(10, TimeUnit.SECONDS)

        then:
        consumedEvents.size() == 1
        consumedEvents.get(0).getInstanceFlowHeaders() == createInstanceFlowHeaders()
        consumedEvents.get(0).getConsumerRecord().value() == testObject
    }

    def 'error event'() {
        given:
        CountDownLatch eventCDL = new CountDownLatch(1)
        ArrayList<InstanceFlowConsumerRecord<ErrorCollection>> consumedEvents = new ArrayList<>()
        def eventConsumer = errorEventConsumerFactory.createRecordFactory(
                (consumerRecord) -> {
                    consumedEvents.add(consumerRecord)
                    eventCDL.countDown()
                }
        ).createContainer(ErrorEventTopicNameParameters.builder().errorEventName("event").build())
        fintListenerBeanRegistrationService.registerBean(eventConsumer)

        when:
        ErrorCollection errorCollection = new ErrorCollection(List.of(
                Error.builder()
                        .errorCode("ERROR_CODE_1")
                        .args(Map.of("arg1", "argValue1", "arg2", "argValue2"))
                        .build(),
                Error.builder()
                        .errorCode("ERROR_CODE_2")
                        .args(Map.of("arg1", "argValue1", "arg2", "argValue2"))
                        .build(),
                Error.builder()
                        .errorCode("ERROR_CODE_3")
                        .args(Map.of("arg1", "argValue1", "arg2", "argValue2"))
                        .build()
        ))

        errorEventProducer.send(
                InstanceFlowErrorEventProducerRecord
                        .builder()
                        .topicNameParameters(ErrorEventTopicNameParameters.builder()
                                .errorEventName("event")
                                .build())
                        .instanceFlowHeaders(createInstanceFlowHeaders())
                        .errorCollection(errorCollection)
                        .build()
        )

        eventCDL.await(10, TimeUnit.SECONDS)

        then:
        consumedEvents.size() == 1
        consumedEvents.get(0).getInstanceFlowHeaders() == createInstanceFlowHeaders()
        consumedEvents.get(0).getConsumerRecord().value() == errorCollection

    }

    def 'entity'() {
        given:
        CountDownLatch entityCDL = new CountDownLatch(1)
        ArrayList<InstanceFlowConsumerRecord<String>> consumedEntities = new ArrayList<>()
        def entityProducer = entityProducerFactory.createProducer(String.class)
        def entityConsumer = entityConsumerFactory.createRecordFactory(
                String.class,
                (consumerRecord) -> {
                    consumedEntities.add(consumerRecord)
                    entityCDL.countDown()
                }
        ).createContainer(EntityTopicNameParameters.builder().resource("resource").build())
        fintListenerBeanRegistrationService.registerBean(entityConsumer)

        when:
        entityProducer.send(
                InstanceFlowEntityProducerRecord.builder()
                        .topicNameParameters(
                                EntityTopicNameParameters.builder()
                                        .resource("resource")
                                        .build()
                        )
                        .instanceFlowHeaders(createInstanceFlowHeaders())
                        .value("valueString")
                        .build()
        )

        entityCDL.await(10, TimeUnit.SECONDS)

        then:
        consumedEntities.size() == 1
        consumedEntities.get(0).getInstanceFlowHeaders() == createInstanceFlowHeaders()
        consumedEntities.get(0).getConsumerRecord().value() == "valueString"
    }

    def 'request reply'() {
        given:
        def requestProducer = requestProducerFactory.createProducer(
                ReplyTopicNameParameters.builder()
                        .applicationId("application")
                        .resource("resource")
                        .build(),
                String.class,
                Integer.class,
                RequestProducerConfiguration
                        .builder()
                        .defaultReplyTimeout(Duration.ofSeconds(10))
                        .build()
        )

        def requestConsumer = requestConsumerFactory.createRecordFactory(
                String.class,
                Integer.class,
                (consumerRecord) -> InstanceFlowReplyProducerRecord.builder()
                        .instanceFlowHeaders(consumerRecord.instanceFlowHeaders)
                        .value(32)
                        .build()
        ).createContainer(RequestTopicNameParameters.builder().resource("resource").build())
        fintListenerBeanRegistrationService.registerBean(requestConsumer)

        when:
        Optional<InstanceFlowConsumerRecord<Integer>> reply = requestProducer.requestAndReceive(
                InstanceFlowRequestProducerRecord.builder()
                        .topicNameParameters(RequestTopicNameParameters.builder()
                                .resource("resource")
                                .build())
                        .instanceFlowHeaders(createInstanceFlowHeaders())
                        .value("requestValueString")
                        .build()
        )

        then:
        reply.isPresent()
        reply.get().getInstanceFlowHeaders() == createInstanceFlowHeaders()
        reply.get().getConsumerRecord().value() == 32
    }

    private createInstanceFlowHeaders() {
        return InstanceFlowHeaders.builder()
                .sourceApplicationId(1)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build()
    }

}
