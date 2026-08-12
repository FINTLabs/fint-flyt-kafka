package no.novari.flyt.kafka.instanceflow.headers

import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord
import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord
import no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestConstructor
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Verifiserer FFS-2222: instansflytens `correlationId` legges som high-cardinality span-attributt
 * (`flyt.correlation-id`) på det aktive sporet der instansflyt-headerne leses og skrives, når
 * `fint.kafka.tracing.enabled=true`.
 */
@SpringBootTest(
    properties = [
        "management.tracing.sampling.probability=1.0",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "fint.kafka.tracing.enabled=true",
    ],
)
@EmbeddedKafka(partitions = 1, kraft = true)
@AutoConfigureObservability
@DirtiesContext
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class InstanceFlowHeadersMapperTracingIntegrationTest(
    private val instanceFlowTemplateFactory: InstanceFlowTemplateFactory,
    private val instanceFlowListenerFactoryService: InstanceFlowListenerFactoryService,
    private val spanExporter: InMemorySpanExporter,
) {
    @TestConfiguration
    class SpanCaptureConfiguration {
        @Bean
        fun inMemorySpanExporter(): InMemorySpanExporter = InMemorySpanExporter.create()

        @Bean
        fun inMemorySpanProcessor(exporter: InMemorySpanExporter): SpanProcessor = SimpleSpanProcessor.create(exporter)
    }

    @BeforeEach
    fun resetSpans() {
        spanExporter.reset()
    }

    private fun createInstanceFlowHeaders(correlationId: UUID): InstanceFlowHeaders =
        InstanceFlowHeaders
            .builder()
            .sourceApplicationId(1L)
            .correlationId(correlationId)
            .build()

    private fun topic(eventName: String): EventTopicNameParameters =
        EventTopicNameParameters
            .builder()
            .topicNamePrefixParameters(
                TopicNamePrefixParameters
                    .stepBuilder()
                    .orgId("test-org-id")
                    .domainContext("test-domain-context")
                    .build(),
            ).eventName(eventName)
            .build()

    @Test
    fun `correlationId is tagged on the producer and consumer spans`() {
        val correlationId = UUID.randomUUID()
        val eventCDL = CountDownLatch(1)
        val consumedEvents = mutableListOf<InstanceFlowConsumerRecord<String>>()
        val topicNameParameters = topic("correlation-id-span")

        val listener =
            instanceFlowListenerFactoryService
                .createRecordListenerContainerFactory(
                    String::class.java,
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
                ).createContainer(topicNameParameters)
        listener.start()

        val record =
            InstanceFlowProducerRecord
                .builder<String>()
                .topicNameParameters(topicNameParameters)
                .instanceFlowHeaders(createInstanceFlowHeaders(correlationId))
                .value("value")
                .build()

        instanceFlowTemplateFactory.createTemplate(String::class.java).send(record)

        val awaitFinished = eventCDL.await(20, TimeUnit.SECONDS)
        assertTrue(awaitFinished, "The count down latch did not count down to zero within the expected time")
        assertEquals(correlationId, consumedEvents.first().instanceFlowHeaders.correlationId)

        listener.stop()

        val spansWithCorrelationId =
            awaitSpans(1).filter { span ->
                span.attributes.asMap().entries.any { (key, value) ->
                    key.key == "flyt.correlation-id" && value == correlationId.toString()
                }
            }

        assertTrue(
            spansWithCorrelationId.isNotEmpty(),
            "expected at least one span tagged with flyt.correlation-id=$correlationId, " +
                "got spans: ${spanExporter.finishedSpanItems.map { it.name to it.attributes }}",
        )
    }

    private fun awaitSpans(minCount: Int): List<SpanData> {
        for (i in 0 until 100) {
            if (spanExporter.finishedSpanItems.size >= minCount) break
            Thread.sleep(100)
        }
        Thread.sleep(200)
        return spanExporter.finishedSpanItems
    }
}
