package no.fintlabs.flyt.kafka;

import no.fintlabs.flyt.kafka.entity.InstanceFlowEntityConsumerFactoryService;
import no.fintlabs.flyt.kafka.entity.InstanceFlowEntityProducerFactory;
import no.fintlabs.flyt.kafka.entity.InstanceFlowEntityProducerRecord;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventConsumerFactoryService;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducer;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerFactory;
import no.fintlabs.flyt.kafka.event.InstanceFlowEventProducerRecord;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventConsumerFactoryService;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducer;
import no.fintlabs.flyt.kafka.event.error.InstanceFlowErrorEventProducerRecord;
import no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders;
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowReplyProducerRecord;
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowRequestConsumerFactoryService;
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowRequestProducerFactory;
import no.fintlabs.flyt.kafka.requestreply.InstanceFlowRequestProducerRecord;
import no.fintlabs.kafka.common.ListenerBeanRegistrationService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import no.fintlabs.kafka.event.error.Error;
import no.fintlabs.kafka.event.error.ErrorCollection;
import no.fintlabs.kafka.event.error.topic.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.event.topic.EventTopicNameParameters;
import no.fintlabs.kafka.requestreply.RequestProducerConfiguration;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
public class InstanceFlowProducerConsumerIntegrationTest {

    @Autowired
    private InstanceFlowEventProducerFactory eventProducerFactory;
    @Autowired
    private InstanceFlowEventConsumerFactoryService eventConsumerFactory;

    @Autowired
    private InstanceFlowErrorEventProducer errorEventProducer;
    @Autowired
    private InstanceFlowErrorEventConsumerFactoryService errorEventConsumerFactory;

    @Autowired
    private InstanceFlowEntityProducerFactory entityProducerFactory;
    @Autowired
    private InstanceFlowEntityConsumerFactoryService entityConsumerFactory;

    @Autowired
    private InstanceFlowRequestProducerFactory requestProducerFactory;
    @Autowired
    private InstanceFlowRequestConsumerFactoryService requestConsumerFactory;

    @Autowired
    private ListenerBeanRegistrationService fintListenerBeanRegistrationService;

    private static class TestObject {
        private Integer integer;
        private String string;

        public Integer getInteger() {
            return integer;
        }

        public void setInteger(Integer integer) {
            this.integer = integer;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return Objects.equals(integer, that.integer) &&
                    Objects.equals(string, that.string);
        }

        @Override
        public int hashCode() {
            return Objects.hash(integer, string);
        }
    }

    @Test
    public void eventTest() throws InterruptedException {
        CountDownLatch eventCDL = new CountDownLatch(1);
        ArrayList<InstanceFlowConsumerRecord<TestObject>> consumedEvents = new ArrayList<>();
        InstanceFlowEventProducer<TestObject> eventProducer = eventProducerFactory.createProducer(TestObject.class);
        var eventConsumer = eventConsumerFactory.createRecordFactory(
                TestObject.class,
                consumerRecord -> {
                    consumedEvents.add(consumerRecord);
                    eventCDL.countDown();
                }
        ).createContainer(EventTopicNameParameters.builder().eventName("event").build());
        fintListenerBeanRegistrationService.registerBean(eventConsumer);

        TestObject testObject = new TestObject();
        testObject.setInteger(2);
        testObject.setString("testObjectString");

        InstanceFlowEventProducerRecord<TestObject> record = InstanceFlowEventProducerRecord.<TestObject>builder()
                        .topicNameParameters(EventTopicNameParameters.builder()
                                .eventName("event")
                                .build())
                        .instanceFlowHeaders(createInstanceFlowHeaders())
                        .value(testObject)
                        .build();
        eventProducer.send(record);

        boolean awaitFinished = eventCDL.await(10, TimeUnit.SECONDS);
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time");

        assertEquals(1, consumedEvents.size());
        assertEquals(createInstanceFlowHeaders(), consumedEvents.get(0).getInstanceFlowHeaders());
        assertEquals(testObject, consumedEvents.get(0).getConsumerRecord().value());
    }

    @Test
    public void errorEventTest() throws InterruptedException {
        CountDownLatch eventCDL = new CountDownLatch(1);
        ArrayList<InstanceFlowConsumerRecord<ErrorCollection>> consumedEvents = new ArrayList<>();
        var eventConsumer = errorEventConsumerFactory.createRecordFactory(
                consumerRecord -> {
                    consumedEvents.add(consumerRecord);
                    eventCDL.countDown();
                }
        ).createContainer(ErrorEventTopicNameParameters.builder().errorEventName("event").build());
        fintListenerBeanRegistrationService.registerBean(eventConsumer);

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
        ));

        InstanceFlowErrorEventProducerRecord record = InstanceFlowErrorEventProducerRecord.builder()
                .topicNameParameters(ErrorEventTopicNameParameters.builder()
                        .errorEventName("event")
                        .build())
                .instanceFlowHeaders(createInstanceFlowHeaders())
                .errorCollection(errorCollection)
                .build();

        errorEventProducer.send(record);

        boolean awaitFinished = eventCDL.await(10, TimeUnit.SECONDS);
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time");

        assertEquals(1, consumedEvents.size());
        assertEquals(createInstanceFlowHeaders(), consumedEvents.get(0).getInstanceFlowHeaders());
        assertEquals(errorCollection, consumedEvents.get(0).getConsumerRecord().value());
    }

    @Test
    public void entityTest() throws InterruptedException {
        CountDownLatch entityCDL = new CountDownLatch(1);
        ArrayList<InstanceFlowConsumerRecord<String>> consumedEntities = new ArrayList<>();
        var entityProducer = entityProducerFactory.createProducer(String.class);
        var entityConsumer = entityConsumerFactory.createRecordFactory(
                String.class,
                consumerRecord -> {
                    consumedEntities.add(consumerRecord);
                    entityCDL.countDown();
                }
        ).createContainer(EntityTopicNameParameters.builder().resource("resource").build());
        fintListenerBeanRegistrationService.registerBean(entityConsumer);

        InstanceFlowEntityProducerRecord<String> record = InstanceFlowEntityProducerRecord.<String>builder()
                .topicNameParameters(
                        EntityTopicNameParameters.builder()
                                .resource("resource")
                                .build()
                )
                .instanceFlowHeaders(createInstanceFlowHeaders())
                .value("valueString")
                .build();

        entityProducer.send(record);

        boolean awaitFinished = entityCDL.await(10, TimeUnit.SECONDS);
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time");

        assertEquals(1, consumedEntities.size());
        assertEquals(createInstanceFlowHeaders(), consumedEntities.get(0).getInstanceFlowHeaders());
        assertEquals("valueString", consumedEntities.get(0).getConsumerRecord().value());
    }

    @Test
    public void requestReplyTest() {
        var requestProducer = requestProducerFactory.createProducer(
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
        );

        var requestConsumer = requestConsumerFactory.createRecordFactory(
                String.class,
                Integer.class,
                (consumerRecord) -> InstanceFlowReplyProducerRecord.<Integer>builder()
                            .instanceFlowHeaders(consumerRecord.getInstanceFlowHeaders())
                            .value(32)
                            .build()
        ).createContainer(RequestTopicNameParameters.builder().resource("resource").build());
        fintListenerBeanRegistrationService.registerBean(requestConsumer);

        Optional<InstanceFlowConsumerRecord<Integer>> reply = requestProducer.requestAndReceive(
                InstanceFlowRequestProducerRecord.<String>builder()
                        .topicNameParameters(RequestTopicNameParameters.builder()
                                .resource("resource")
                                .build())
                        .instanceFlowHeaders(createInstanceFlowHeaders())
                        .value("requestValueString")
                        .build()
        );

        assertTrue(reply.isPresent(), "Reply should be present");
        assertEquals(createInstanceFlowHeaders(), reply.get().getInstanceFlowHeaders());
        assertEquals(32, reply.get().getConsumerRecord().value());
    }

    private InstanceFlowHeaders createInstanceFlowHeaders() {
        return InstanceFlowHeaders.builder()
                .sourceApplicationId(1L)
                .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
                .sourceApplicationInstanceId("sourceApplicationInstanceId")
                .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
                .build();
    }
}
