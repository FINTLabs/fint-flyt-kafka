package no.novari.flyt.kafka.instanceflow.consuming

import no.novari.flyt.kafka.instanceflow.headers.NoInstanceFlowHeadersException
import no.novari.kafka.consuming.ErrorHandlerConfiguration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.util.backoff.BackOff
import java.time.Duration
import java.util.function.BiFunction

class InstanceFlowErrorHandlerConfigurationMapperTest {
    private lateinit var instanceFlowConsumerRecordMapper: InstanceFlowConsumerRecordMapper
    private lateinit var instanceFlowErrorHandlerConfigurationMapper: InstanceFlowErrorHandlerConfigurationMapper

    @BeforeEach
    fun setUp() {
        instanceFlowConsumerRecordMapper = mock()
        instanceFlowErrorHandlerConfigurationMapper =
            InstanceFlowErrorHandlerConfigurationMapper(instanceFlowConsumerRecordMapper)
    }

    @Test
    fun `maps no-retry no-recovery configuration through to ErrorHandlerConfiguration`() {
        val errorHandlerConfiguration =
            instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                InstanceFlowErrorHandlerConfiguration
                    .stepBuilder<String>()
                    .noRetries()
                    .skipFailedRecords()
                    .build(),
            )

        assertThat(errorHandlerConfiguration)
            .usingRecursiveComparison()
            .isEqualTo(
                ErrorHandlerConfiguration
                    .stepBuilder<String>()
                    .noRetries()
                    .skipFailedRecords()
                    .build(),
            )
        verifyNoInteractions(instanceFlowConsumerRecordMapper)
    }

    @Test
    fun `maps retry configuration through to ErrorHandlerConfiguration`() {
        val errorHandlerConfiguration =
            instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                InstanceFlowErrorHandlerConfiguration
                    .stepBuilder<String>()
                    .retryWithFixedInterval(Duration.ofSeconds(20), 5)
                    .excludeExceptionsFromRetry(listOf(IllegalArgumentException::class.java))
                    .restartRetryOnExceptionChange()
                    .skipFailedRecords()
                    .build(),
            )

        assertThat(errorHandlerConfiguration)
            .usingRecursiveComparison()
            .isEqualTo(
                ErrorHandlerConfiguration
                    .stepBuilder<String>()
                    .retryWithFixedInterval(Duration.ofSeconds(20), 5)
                    .excludeExceptionsFromRetry(listOf(IllegalArgumentException::class.java))
                    .restartRetryOnExceptionChange()
                    .skipFailedRecords()
                    .build(),
            )
        verifyNoInteractions(instanceFlowConsumerRecordMapper)
    }

    @Test
    fun `wraps instance-flow backoff function and delegates to inner function on use`() {
        val instanceFlowBackOffFunction: BiFunction<InstanceFlowConsumerRecord<String>, Exception, BackOff?> = mock()
        whenever(instanceFlowBackOffFunction.apply(any(), any())).thenReturn(mock<BackOff>())

        val errorHandlerConfiguration =
            instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                InstanceFlowErrorHandlerConfiguration
                    .stepBuilder<String>()
                    .retryWithBackoffFunction(instanceFlowBackOffFunction)
                    .orElse()
                    .retryWithFixedInterval(Duration.ofSeconds(20), 5)
                    .excludeExceptionsFromRetry(listOf(IllegalArgumentException::class.java))
                    .restartRetryOnExceptionChange()
                    .skipFailedRecords()
                    .build(),
            )

        assertThat(errorHandlerConfiguration)
            .usingRecursiveComparison()
            .ignoringFields("backOffFunction")
            .isEqualTo(
                ErrorHandlerConfiguration
                    .stepBuilder<String>()
                    .retryWithFixedInterval(Duration.ofSeconds(20), 5)
                    .excludeExceptionsFromRetry(listOf(IllegalArgumentException::class.java))
                    .restartRetryOnExceptionChange()
                    .skipFailedRecords()
                    .build(),
            )
        assertThat(errorHandlerConfiguration.backOffFunction).isNotEmpty

        val consumerRecord = mock<ConsumerRecord<String, String>>()
        val instanceFlowConsumerRecord = mock<InstanceFlowConsumerRecord<String>>()
        whenever(instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(consumerRecord))
            .thenReturn(instanceFlowConsumerRecord)

        val illegalStateException = mock<IllegalStateException>()
        errorHandlerConfiguration.backOffFunction.get().apply(consumerRecord, illegalStateException)

        verify(instanceFlowBackOffFunction).apply(instanceFlowConsumerRecord, illegalStateException)
        verify(instanceFlowConsumerRecordMapper).toInstanceFlowConsumerRecord(consumerRecord)
        verifyNoMoreInteractions(instanceFlowBackOffFunction, instanceFlowConsumerRecordMapper)
    }

    @Test
    fun `wraps custom instance-flow recoverer and routes missing-header failures to the missing-header callback`() {
        val instanceFlowRecoverer: InstanceFlowErrorHandlerConfiguration.InstanceFlowRecoverer<String> = mock()

        val errorHandlerConfiguration =
            instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                InstanceFlowErrorHandlerConfiguration
                    .stepBuilder<String>()
                    .noRetries()
                    .recoverFailedRecords(instanceFlowRecoverer)
                    .reprocessAndRetryRecordOnRecoveryFailure()
                    .build(),
            )

        assertThat(errorHandlerConfiguration)
            .usingRecursiveComparison()
            .ignoringFields("customRecoverer")
            .isEqualTo(
                ErrorHandlerConfiguration
                    .stepBuilder<String>()
                    .noRetries()
                    .recoverFailedRecords { _, _ -> }
                    .reprocessAndRetryRecordOnRecoveryFailure()
                    .build(),
            )
        assertThat(errorHandlerConfiguration.customRecoverer).isNotEmpty

        val consumerRecord1 = mock<ConsumerRecord<String, String>>()
        val consumerRecord2 = mock<ConsumerRecord<String, String>>()
        val instanceFlowConsumerRecord = mock<InstanceFlowConsumerRecord<String>>()
        whenever(instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(consumerRecord1))
            .thenReturn(instanceFlowConsumerRecord)
        whenever(instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(consumerRecord2))
            .thenThrow(NoInstanceFlowHeadersException::class.java)

        val illegalStateException = mock<IllegalStateException>()

        errorHandlerConfiguration.customRecoverer.get().accept(consumerRecord1, null, illegalStateException)
        verify(instanceFlowRecoverer).recover(instanceFlowConsumerRecord, illegalStateException)
        verify(instanceFlowConsumerRecordMapper).toInstanceFlowConsumerRecord(consumerRecord1)

        errorHandlerConfiguration.customRecoverer.get().accept(consumerRecord2, null, illegalStateException)
        verify(instanceFlowRecoverer).recoverForMissingInstanceFlowHeaders(consumerRecord2, illegalStateException)
        verify(instanceFlowConsumerRecordMapper).toInstanceFlowConsumerRecord(consumerRecord2)

        verifyNoMoreInteractions(instanceFlowRecoverer, instanceFlowConsumerRecordMapper)
    }
}
