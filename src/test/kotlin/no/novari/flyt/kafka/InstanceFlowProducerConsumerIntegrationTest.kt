package no.novari.flyt.kafka

import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory
import no.novari.flyt.kafka.model.Error
import no.novari.flyt.kafka.model.ErrorCollection
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.topic.name.EntityTopicNameParameters
import no.novari.kafka.topic.name.ErrorEventTopicNameParameters
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestConstructor
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest
@EmbeddedKafka
@DirtiesContext
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class InstanceFlowProducerConsumerIntegrationTest(
    private val instanceFlowTemplateFactory: InstanceFlowTemplateFactory,
    private val instanceFlowListenerFactoryService: InstanceFlowListenerFactoryService,
) {
    private data class TestObject(
        val integer: Int?,
        val string: String?,
    )

    @Test
    fun `event roundtrips through Kafka with instance flow headers and value preserved`() {
        val eventCDL = CountDownLatch(1)
        val consumedEvents = mutableListOf<InstanceFlowConsumerRecord<TestObject>>()
        val listener =
            instanceFlowListenerFactoryService
                .createRecordListenerContainerFactory(
                    TestObject::class.java,
                    { consumerRecord ->
                        consumedEvents.add(consumerRecord)
                        eventCDL.countDown()
                    },
                    ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                    null,
                ).createContainer(
                    EventTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(
                            TopicNamePrefixParameters
                                .stepBuilder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build(),
                        ).eventName("event")
                        .build(),
                )
        listener.start()

        val testObject = TestObject(2, "testObjectString")

        val record =
            InstanceFlowProducerRecord
                .builder<TestObject>()
                .topicNameParameters(
                    EventTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(
                            TopicNamePrefixParameters
                                .stepBuilder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build(),
                        ).eventName("event")
                        .build(),
                ).instanceFlowHeaders(createInstanceFlowHeaders())
                .value(testObject)
                .build()

        val template = instanceFlowTemplateFactory.createTemplate(TestObject::class.java)
        template.send(record)

        val awaitFinished = eventCDL.await(10, TimeUnit.SECONDS)
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time")

        assertEquals(1, consumedEvents.size)
        assertEquals(createInstanceFlowHeaders(), consumedEvents.first().instanceFlowHeaders)
        assertEquals(testObject, consumedEvents.first().consumerRecord.value())
    }

    @Test
    fun `error event roundtrips through Kafka with ErrorCollection payload preserved`() {
        val eventCDL = CountDownLatch(1)
        val consumedEvents = mutableListOf<InstanceFlowConsumerRecord<ErrorCollection>>()
        val listener =
            instanceFlowListenerFactoryService
                .createRecordListenerContainerFactory(
                    ErrorCollection::class.java,
                    { consumerRecord ->
                        consumedEvents.add(consumerRecord)
                        eventCDL.countDown()
                    },
                    ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                    null,
                ).createContainer(
                    ErrorEventTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(
                            TopicNamePrefixParameters
                                .stepBuilder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build(),
                        ).errorEventName("event")
                        .build(),
                )
        listener.start()

        val errorCollection =
            ErrorCollection(
                listOf(
                    Error
                        .builder()
                        .errorCode(
                            "ERROR_CODE_1",
                        ).args(mapOf("arg1" to "argValue1", "arg2" to "argValue2"))
                        .build(),
                    Error
                        .builder()
                        .errorCode(
                            "ERROR_CODE_2",
                        ).args(mapOf("arg1" to "argValue1", "arg2" to "argValue2"))
                        .build(),
                    Error
                        .builder()
                        .errorCode(
                            "ERROR_CODE_3",
                        ).args(mapOf("arg1" to "argValue1", "arg2" to "argValue2"))
                        .build(),
                ),
            )

        val record =
            InstanceFlowProducerRecord
                .builder<ErrorCollection>()
                .topicNameParameters(
                    ErrorEventTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(
                            TopicNamePrefixParameters
                                .stepBuilder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build(),
                        ).errorEventName("event")
                        .build(),
                ).instanceFlowHeaders(createInstanceFlowHeaders())
                .value(errorCollection)
                .build()

        val template = instanceFlowTemplateFactory.createTemplate(ErrorCollection::class.java)
        template.send(record)

        val awaitFinished = eventCDL.await(10, TimeUnit.SECONDS)
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time")

        assertEquals(1, consumedEvents.size)
        assertEquals(createInstanceFlowHeaders(), consumedEvents.first().instanceFlowHeaders)
        assertEquals(errorCollection, consumedEvents.first().consumerRecord.value())
    }

    @Test
    fun `entity roundtrips through Kafka with String payload preserved`() {
        val entityCDL = CountDownLatch(1)
        val consumedEntities = mutableListOf<InstanceFlowConsumerRecord<String>>()
        val entityProducer = instanceFlowTemplateFactory.createTemplate(String::class.java)
        val entityConsumer =
            instanceFlowListenerFactoryService
                .createRecordListenerContainerFactory(
                    String::class.java,
                    { consumerRecord ->
                        consumedEntities.add(consumerRecord)
                        entityCDL.countDown()
                    },
                    ListenerConfiguration
                        .stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .continueFromPreviousOffsetOnAssignment()
                        .build(),
                    null,
                ).createContainer(
                    EntityTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(
                            TopicNamePrefixParameters
                                .stepBuilder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build(),
                        ).resourceName("resource")
                        .build(),
                )
        entityConsumer.start()

        val record =
            InstanceFlowProducerRecord
                .builder<String>()
                .topicNameParameters(
                    EntityTopicNameParameters
                        .builder()
                        .topicNamePrefixParameters(
                            TopicNamePrefixParameters
                                .stepBuilder()
                                .orgId("test-org-id")
                                .domainContext("test-domain-context")
                                .build(),
                        ).resourceName("resource")
                        .build(),
                ).instanceFlowHeaders(createInstanceFlowHeaders())
                .value("valueString")
                .build()

        entityProducer.send(record)

        val awaitFinished = entityCDL.await(10, TimeUnit.SECONDS)
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time")

        assertEquals(1, consumedEntities.size)
        assertEquals(createInstanceFlowHeaders(), consumedEntities.first().instanceFlowHeaders)
        assertEquals("valueString", consumedEntities.first().consumerRecord.value())
    }

    private fun createInstanceFlowHeaders(): InstanceFlowHeaders =
        InstanceFlowHeaders
            .builder()
            .sourceApplicationId(1L)
            .sourceApplicationIntegrationId("sourceApplicationIntegrationId")
            .sourceApplicationInstanceId("sourceApplicationInstanceId")
            .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
            .build()
}
