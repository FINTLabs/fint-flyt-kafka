package no.fintlabs.flyt.kafka;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.ResourceUtils;

@Configuration
@PropertySource(ResourceUtils.CLASSPATH_URL_PREFIX + "application-flyt-kafka.properties")
public class FlytKafkaConfiguration {
}
