package no.novari.flyt.kafka.instanceflow.consuming;

import no.novari.flyt.kafka.instanceflow.headers.NoInstanceFlowHeadersException;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.backoff.BackOff;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class InstanceFlowErrorHandlerConfigurationMapperTest {

    InstanceFlowConsumerRecordMapper instanceFlowConsumerRecordMapper;
    InstanceFlowErrorHandlerConfigurationMapper instanceFlowErrorHandlerConfigurationMapper;

    @BeforeEach
    void setUp() {
        instanceFlowConsumerRecordMapper = mock(InstanceFlowConsumerRecordMapper.class);
        instanceFlowErrorHandlerConfigurationMapper = new InstanceFlowErrorHandlerConfigurationMapper(
                instanceFlowConsumerRecordMapper
        );
    }

    @Test
    void givenNoRetryAndNoCustomRecoveryShouldMapToSameSettingsInErrorHandlerConfiguration() {
        ErrorHandlerConfiguration<String> errorHandlerConfiguration =
                instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                        InstanceFlowErrorHandlerConfiguration
                                .<String>stepBuilder()
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                );
        assertThat(errorHandlerConfiguration)
                .usingRecursiveComparison()
                .isEqualTo(
                        ErrorHandlerConfiguration
                                .stepBuilder()
                                .noRetries()
                                .skipFailedRecords()
                                .build()
                );
        verifyNoInteractions(instanceFlowConsumerRecordMapper);
    }

    @Test
    void givenRetryAndNoCustomRecoveryShouldMapToSameSettingsInErrorHandlerConfiguration() {
        ErrorHandlerConfiguration<String> errorHandlerConfiguration =
                instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                        InstanceFlowErrorHandlerConfiguration
                                .<String>stepBuilder()
                                .retryWithFixedInterval(Duration.ofSeconds(20), 5)
                                .excludeExceptionsFromRetry(List.of(IllegalArgumentException.class))
                                .restartRetryOnExceptionChange()
                                .skipFailedRecords()
                                .build()
                );
        assertThat(errorHandlerConfiguration)
                .usingRecursiveComparison()
                .isEqualTo(
                        ErrorHandlerConfiguration
                                .stepBuilder()
                                .retryWithFixedInterval(Duration.ofSeconds(20), 5)
                                .excludeExceptionsFromRetry(List.of(IllegalArgumentException.class))
                                .restartRetryOnExceptionChange()
                                .skipFailedRecords()
                                .build()
                );
        verifyNoInteractions(instanceFlowConsumerRecordMapper);
    }

    @Test
    void givenInstanceFlowBackoffFunctionShouldWrapBackOffFunctionAndCallInnerOnUse() {
        BiFunction<InstanceFlowConsumerRecord<String>, Exception, Optional<BackOff>> instanceFlowBackOffFunction =
                mock(BiFunction.class);
        when(instanceFlowBackOffFunction.apply(any(), any())).thenReturn(Optional.of(mock(BackOff.class)));

        ErrorHandlerConfiguration<String> errorHandlerConfiguration =
                instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                        InstanceFlowErrorHandlerConfiguration
                                .<String>stepBuilder()
                                .retryWithBackoffFunction(instanceFlowBackOffFunction)
                                .orElse()
                                .retryWithFixedInterval(Duration.ofSeconds(20), 5)
                                .excludeExceptionsFromRetry(List.of(IllegalArgumentException.class))
                                .restartRetryOnExceptionChange()
                                .skipFailedRecords()
                                .build()
                );
        assertThat(errorHandlerConfiguration)
                .usingRecursiveComparison()
                .ignoringFields("backOffFunction")
                .isEqualTo(
                        ErrorHandlerConfiguration
                                .stepBuilder()
                                .retryWithFixedInterval(Duration.ofSeconds(20), 5)
                                .excludeExceptionsFromRetry(List.of(IllegalArgumentException.class))
                                .restartRetryOnExceptionChange()
                                .skipFailedRecords()
                                .build()
                );
        assertThat(errorHandlerConfiguration.getBackOffFunction()).isNotEmpty();

        ConsumerRecord<String, String> consumerRecord1 = mock(ConsumerRecord.class);
        InstanceFlowConsumerRecord<String> instanceFlowConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(consumerRecord1))
                .thenReturn(instanceFlowConsumerRecord);

        IllegalStateException illegalStateException = mock(IllegalStateException.class);
        errorHandlerConfiguration
                .getBackOffFunction()
                .get()
                .apply(consumerRecord1, illegalStateException);

        verify(instanceFlowBackOffFunction).apply(instanceFlowConsumerRecord, illegalStateException);
        verify(instanceFlowConsumerRecordMapper).toInstanceFlowConsumerRecord(consumerRecord1);
        verifyNoMoreInteractions(
                instanceFlowBackOffFunction,
                instanceFlowConsumerRecordMapper
        );
    }

    @Test
    void givenCustomInstanceFlowRecovererShouldWrapRecovererAndCallInnerOnUse() {
        InstanceFlowErrorHandlerConfiguration.InstanceFlowRecoverer<String> instanceFlowRecoverer =
                mock(InstanceFlowErrorHandlerConfiguration.InstanceFlowRecoverer.class);

        ErrorHandlerConfiguration<String> errorHandlerConfiguration =
                instanceFlowErrorHandlerConfigurationMapper.toErrorHandlerConfiguration(
                        InstanceFlowErrorHandlerConfiguration
                                .<String>stepBuilder()
                                .noRetries()
                                .recoverFailedRecords(instanceFlowRecoverer)
                                .reprocessAndRetryRecordOnRecoveryFailure()
                                .build()
                );
        assertThat(errorHandlerConfiguration)
                .usingRecursiveComparison()
                .ignoringFields("customRecoverer")
                .isEqualTo(
                        ErrorHandlerConfiguration
                                .stepBuilder()
                                .noRetries()
                                .recoverFailedRecords(((stringObjectConsumerRecord, e) -> {}))
                                .reprocessAndRetryRecordOnRecoveryFailure()
                                .build()
                );
        assertThat(errorHandlerConfiguration.getCustomRecoverer()).isNotEmpty();


        ConsumerRecord<String, String> consumerRecord1 = mock(ConsumerRecord.class);
        ConsumerRecord<String, String> consumerRecord2 = mock(ConsumerRecord.class);
        InstanceFlowConsumerRecord<String> instanceFlowConsumerRecord = mock(InstanceFlowConsumerRecord.class);
        when(instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(consumerRecord1))
                .thenReturn(instanceFlowConsumerRecord);
        when(instanceFlowConsumerRecordMapper.toInstanceFlowConsumerRecord(consumerRecord2))
                .thenThrow(NoInstanceFlowHeadersException.class);

        IllegalStateException illegalStateException = mock(IllegalStateException.class);

        errorHandlerConfiguration
                .getCustomRecoverer()
                .get()
                .accept(consumerRecord1, null, illegalStateException);
        verify(instanceFlowRecoverer).recover(instanceFlowConsumerRecord, illegalStateException);
        verify(instanceFlowConsumerRecordMapper).toInstanceFlowConsumerRecord(consumerRecord1);

        errorHandlerConfiguration
                .getCustomRecoverer()
                .get()
                .accept(consumerRecord2, null, illegalStateException);
        verify(instanceFlowRecoverer).recoverForMissingInstanceFlowHeaders(consumerRecord2, illegalStateException);
        verify(instanceFlowConsumerRecordMapper).toInstanceFlowConsumerRecord(consumerRecord2);

        verifyNoMoreInteractions(
                instanceFlowRecoverer,
                instanceFlowConsumerRecordMapper
        );
    }
}
