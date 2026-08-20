package no.novari.flyt.kafka.instanceflow.headers

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.apache.kafka.common.header.internals.RecordHeader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.ObjectProvider
import java.util.UUID
import java.util.function.Consumer

class InstanceFlowHeadersMapperTest {
    private lateinit var objectMapper: ObjectMapper
    private lateinit var observationRegistry: ObjectProvider<ObservationRegistry>
    private lateinit var instanceFlowHeadersMapper: InstanceFlowHeadersMapper

    private val instanceFlowHeaders =
        InstanceFlowHeaders
            .builder()
            .sourceApplicationId(1L)
            .correlationId(UUID.fromString("2ee6f95e-44c3-11ed-b878-0242ac120002"))
            .build()

    @BeforeEach
    fun setUp() {
        objectMapper = mock()
        observationRegistry = mock()
        instanceFlowHeadersMapper = InstanceFlowHeadersMapper(objectMapper, observationRegistry)

        whenever(objectMapper.writeValueAsBytes(any())).thenReturn(byteArrayOf(1, 2, 3))
        whenever(objectMapper.readValue(any<ByteArray>(), eq(InstanceFlowHeaders::class.java)))
            .thenReturn(instanceFlowHeaders)
    }

    private fun stubObservationRegistry(registry: ObservationRegistry) {
        whenever(observationRegistry.ifAvailable(any())).thenAnswer { invocation ->
            invocation.getArgument<Consumer<ObservationRegistry>>(0).accept(registry)
        }
    }

    @Test
    fun `toHeader does not fail when no ObservationRegistry bean is available`() {
        val header = instanceFlowHeadersMapper.toHeader(instanceFlowHeaders)

        assertThat(header).isNotNull
        verify(observationRegistry).ifAvailable(any())
    }

    @Test
    fun `toHeader does not fail when no observation is currently active`() {
        val registry = mock<ObservationRegistry>()
        whenever(registry.currentObservation).thenReturn(null)
        stubObservationRegistry(registry)

        instanceFlowHeadersMapper.toHeader(instanceFlowHeaders)

        verify(registry).currentObservation
    }

    @Test
    fun `toHeader tags the currently active observation with the correlationId`() {
        val registry = mock<ObservationRegistry>()
        val observation = mock<Observation>()
        whenever(registry.currentObservation).thenReturn(observation)
        stubObservationRegistry(registry)

        instanceFlowHeadersMapper.toHeader(instanceFlowHeaders)

        verify(observation).highCardinalityKeyValue(
            "flyt.correlation-id",
            "2ee6f95e-44c3-11ed-b878-0242ac120002",
        )
    }

    @Test
    fun `toInstanceFlowHeaders tags the currently active observation with the correlationId`() {
        val registry = mock<ObservationRegistry>()
        val observation = mock<Observation>()
        whenever(registry.currentObservation).thenReturn(observation)
        stubObservationRegistry(registry)

        val result = instanceFlowHeadersMapper.toInstanceFlowHeaders(RecordHeader("key", byteArrayOf(1, 2, 3)))

        assertThat(result).isEqualTo(instanceFlowHeaders)
        verify(observation).highCardinalityKeyValue(
            "flyt.correlation-id",
            "2ee6f95e-44c3-11ed-b878-0242ac120002",
        )
    }
}
