package no.novari.flyt.kafka.instanceflow.consuming;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.util.backoff.BackOff;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
public class InstanceFlowErrorHandlerConfiguration<VALUE> {

    public static <VALUE> InstanceFlowErrorHandlerConfigurationStepBuilder.RetryStep<VALUE> stepBuilder() {
        return InstanceFlowErrorHandlerConfigurationStepBuilder.firstStep();
    }

    private final BiFunction<InstanceFlowConsumerRecord<VALUE>, Exception, Optional<BackOff>> backOffFunction;

    private final BackOff defaultBackoff;

    public interface InstanceFlowRecoverer<VALUE> {
        void recover(InstanceFlowConsumerRecord<VALUE> instanceFlowConsumerRecord, Exception exception);

        void recoverForMissingInstanceFlowHeaders(ConsumerRecord<String, VALUE> consumerRecord, Exception exception);
    }

    private final InstanceFlowRecoverer<VALUE> customRecoverer;

    @Getter
    @Builder.Default
    private final ErrorHandlerConfiguration.ClassificationType classificationType =
            ErrorHandlerConfiguration.ClassificationType.DEFAULT;

    @Getter
    private final Collection<Class<? extends Exception>> classificationExceptions;

    @Getter
    @Builder.Default
    private final boolean skipRecordOnRecoveryFailure = false;

    @Getter
    @Builder.Default
    private final boolean restartRetryOnExceptionChange = true;

    @Getter
    @Builder.Default
    private final boolean restartRetryOnRecoveryFailure = true;

    public Optional<BackOff> getDefaultBackoff() {
        return Optional.ofNullable(defaultBackoff);
    }

    public Optional<BiFunction<InstanceFlowConsumerRecord<VALUE>, Exception, Optional<BackOff>>> getBackOffFunction() {
        return Optional.ofNullable(backOffFunction);
    }

    public Optional<InstanceFlowRecoverer<VALUE>> getCustomRecoverer() {
        return Optional.ofNullable(customRecoverer);
    }

}
