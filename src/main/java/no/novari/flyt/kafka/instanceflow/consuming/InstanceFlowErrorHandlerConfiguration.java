package no.novari.flyt.kafka.instanceflow.consuming;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.logging.log4j.util.TriConsumer;
import org.springframework.util.backoff.BackOff;

import java.util.Optional;
import java.util.function.BiFunction;

@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InstanceFlowErrorHandlerConfiguration<VALUE> {

    public static <VALUE> InstanceFlowErrorHandlerConfigurationStepBuilder.RetryStep<VALUE> stepBuilder(
            Class<VALUE> consumerRecordValueClass
    ) {
        return InstanceFlowErrorHandlerConfigurationStepBuilder.firstStep(consumerRecordValueClass);
    }

    @Getter
    private final Class<VALUE> consumerRecordValueClass;

    private final BiFunction<InstanceFlowConsumerRecord<VALUE>, Exception, Optional<BackOff>> backOffFunction;

    private final BackOff defaultBackoff;

    private final TriConsumer<InstanceFlowConsumerRecord<VALUE>, Consumer<String, VALUE>, Exception> recoverer;

    public Optional<BackOff> getDefaultBackoff() {
        return Optional.ofNullable(defaultBackoff);
    }

    public Optional<BiFunction<InstanceFlowConsumerRecord<VALUE>, Exception, Optional<BackOff>>> getBackOffFunction() {
        return Optional.ofNullable(backOffFunction);
    }

    public Optional<TriConsumer<InstanceFlowConsumerRecord<VALUE>, Consumer<String, VALUE>, Exception>> getRecoverer() {
        return Optional.ofNullable(recoverer);
    }

}
