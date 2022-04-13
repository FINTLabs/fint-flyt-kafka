package no.fintlabs.flyt.kafka;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.ResourceUtils;

@EnableAutoConfiguration
@Configuration
@PropertySource(ResourceUtils.CLASSPATH_URL_PREFIX + "application-flyt-kafka.properties")
public class FlytKafkaConfiguration {
}
