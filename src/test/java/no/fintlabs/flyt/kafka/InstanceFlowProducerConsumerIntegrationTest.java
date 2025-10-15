package no.fintlabs.flyt.kafka;

import no.fintlabs.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord;
import no.fintlabs.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService;
import no.fintlabs.flyt.kafka.instanceflow.headers.InstanceFlowHeaders;
import no.fintlabs.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord;
import no.fintlabs.flyt.kafka.instanceflow.producing.InstanceFlowTemplate;
import no.fintlabs.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory;
import no.fintlabs.kafka.consuming.ListenerConfiguration;
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

    private final InstanceFlowTemplateFactory instanceFlowTemplateFactory;
    private final InstanceFlowListenerFactoryService instanceFlowListenerFactoryService;

    public InstanceFlowProducerConsumerIntegrationTest(
            @Autowired InstanceFlowTemplateFactory instanceFlowTemplateFactory,
            @Autowired InstanceFlowListenerFactoryService instanceFlowListenerFactoryService
    ) {
        this.instanceFlowTemplateFactory = instanceFlowTemplateFactory;
        this.instanceFlowListenerFactoryService = instanceFlowListenerFactoryService;
    }

    private record TestObject(Integer integer, String string) {
    }

    @Test
    public void eventTest() throws InterruptedException {
        CountDownLatch eventCDL = new CountDownLatch(1);
        ArrayList<InstanceFlowConsumerRecord<TestObject>> consumedEvents = new ArrayList<>();
        var listener = instanceFlowListenerFactoryService.createRecordListenerContainerFactory(
                TestObject.class,
                consumerRecord -> {
                    consumedEvents.add(consumerRecord);
                    eventCDL.countDown();
                },
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                null
        ).createContainer(EventTopicNameParameters.builder()
                .topicNamePrefixParameters(
                        TopicNamePrefixParameters.builder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build()
                )
                .eventName("event")
                .build());
        listener.start();

        TestObject testObject = new TestObject(2, "testObjectString");

        InstanceFlowProducerRecord<TestObject> record = InstanceFlowProducerRecord.<TestObject>builder()
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

        InstanceFlowTemplate<TestObject> template = instanceFlowTemplateFactory.createTemplate(TestObject.class);
        template.send(record);

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
        var listener = instanceFlowListenerFactoryService.createRecordListenerContainerFactory(
                ErrorCollection.class,
                consumerRecord -> {
                    consumedEvents.add(consumerRecord);
                    eventCDL.countDown();
                },
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                null
        ).createContainer(ErrorEventTopicNameParameters.builder()
                .topicNamePrefixParameters(
                        TopicNamePrefixParameters.builder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build()
                )
                .errorEventName("event")
                .build());
        listener.start();

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

        InstanceFlowProducerRecord<ErrorCollection> record = InstanceFlowProducerRecord.<ErrorCollection>builder()
                .topicNameParameters(ErrorEventTopicNameParameters.builder()
                        .topicNamePrefixParameters(
                                TopicNamePrefixParameters.builder()
                                        .orgId("test-org-id")
                                        .domainContext("test-domain-context")
                                        .build()
                        ).errorEventName("event")
                        .build())
                .instanceFlowHeaders(createInstanceFlowHeaders())
                .value(errorCollection)
                .build();

        InstanceFlowTemplate<ErrorCollection> template = instanceFlowTemplateFactory.createTemplate(ErrorCollection.class);
        template.send(record);

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
        var entityProducer = instanceFlowTemplateFactory.createTemplate(String.class);
        var entityConsumer = instanceFlowListenerFactoryService.createRecordListenerContainerFactory(
                String.class,
                consumerRecord -> {
                    consumedEntities.add(consumerRecord);
                    entityCDL.countDown();
                },
                ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                null
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

        InstanceFlowProducerRecord<String> record = InstanceFlowProducerRecord.<String>builder()
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
