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
import no.fintlabs.kafka.model.Error;
import no.fintlabs.kafka.model.ErrorCollection;
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters;
import no.fintlabs.kafka.topic.name.ErrorEventTopicNameParameters;
import no.fintlabs.kafka.topic.name.EventTopicNameParameters;
import no.fintlabs.kafka.topic.name.TopicNamePrefixParameters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "fint.kafka.topic.defaults.org-id=test-org-id",
        "fint.kafka.topic.defaults.domain-context=test-domain-context"
})
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

    private record TestObject(Integer integer, String string) {
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
        ).createContainer(EventTopicNameParameters.builder()
                .topicNamePrefixParameters(
                        TopicNamePrefixParameters.builder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build()
                )
                .eventName("event")
                .build());
        eventConsumer.start();

        TestObject testObject = new TestObject(2, "testObjectString");

        InstanceFlowEventProducerRecord<TestObject> record = InstanceFlowEventProducerRecord.<TestObject>builder()
                .topicNameParameters(EventTopicNameParameters.builder()
                        .topicNamePrefixParameters(
                                TopicNamePrefixParameters.builder()
                                        .orgId("test-org-id")
                                        .domainContext("test-domain-context")
                                        .build()
                        )
                        .eventName("event")
                        .build())
                .instanceFlowHeaders(createInstanceFlowHeaders())
                .value(testObject)
                .build();
        eventProducer.send(record);

        boolean awaitFinished = eventCDL.await(10, TimeUnit.SECONDS);
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time");

        assertEquals(1, consumedEvents.size());
        assertEquals(createInstanceFlowHeaders(), consumedEvents.getFirst().instanceFlowHeaders());
        assertEquals(testObject, consumedEvents.getFirst().consumerRecord().value());
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
        ).createContainer(ErrorEventTopicNameParameters.builder()
                .topicNamePrefixParameters(
                        TopicNamePrefixParameters.builder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build()
                )
                .errorEventName("event")
                .build());
        eventConsumer.start();

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
                        .topicNamePrefixParameters(
                                TopicNamePrefixParameters.builder()
                                        .orgId("test-org-id")
                                        .domainContext("test-domain-context")
                                        .build()
                        ).errorEventName("event")
                        .build())
                .instanceFlowHeaders(createInstanceFlowHeaders())
                .errorCollection(errorCollection)
                .build();

        errorEventProducer.send(record);

        boolean awaitFinished = eventCDL.await(10, TimeUnit.SECONDS);
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time");

        assertEquals(1, consumedEvents.size());
        assertEquals(createInstanceFlowHeaders(), consumedEvents.getFirst().instanceFlowHeaders());
        assertEquals(errorCollection, consumedEvents.getFirst().consumerRecord().value());
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
        ).createContainer(EntityTopicNameParameters.builder()
                .topicNamePrefixParameters(
                        TopicNamePrefixParameters.builder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build()
                )
                .resourceName("resource")
                .build());
        entityConsumer.start();

        InstanceFlowEntityProducerRecord<String> record = InstanceFlowEntityProducerRecord.<String>builder()
                .topicNameParameters(
                        EntityTopicNameParameters.builder()
                                .topicNamePrefixParameters(
                                        TopicNamePrefixParameters.builder()
                                                .orgId("test-org-id")
                                                .domainContext("test-domain-context")
                                                .build()
                                )
                                .resourceName("resource")
                                .build()
                )
                .instanceFlowHeaders(createInstanceFlowHeaders())
                .value("valueString")
                .build();

        entityProducer.send(record);

        boolean awaitFinished = entityCDL.await(10, TimeUnit.SECONDS);
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time");

        assertEquals(1, consumedEntities.size());
        assertEquals(createInstanceFlowHeaders(), consumedEntities.getFirst().instanceFlowHeaders());
        assertEquals("valueString", consumedEntities.getFirst().consumerRecord().value());
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
