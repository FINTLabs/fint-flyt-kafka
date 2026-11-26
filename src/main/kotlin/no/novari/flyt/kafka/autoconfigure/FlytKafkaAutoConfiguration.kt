package no.novari.flyt.kafka.autoconfigure

import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersConverter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
internal class FlytKafkaAutoConfiguration {
    @Bean
    fun instanceFlowHeadersConverter() = InstanceFlowHeadersConverter()
}
