# FINT Flyt Kafka Project
[![CI](https://github.com/FINTLabs/fint-flyt-kafka/actions/workflows/ci.yaml/badge.svg)](https://github.com/FINTLabs/fint-flyt-kafka/actions/workflows/ci.yaml)

This library expands on the FINT Kafka library (fint-kafka) by configuring application properties and adding an api for producing and consuming kafka messages with instance flow headers. This readme only covers the added functionality. For information on the basic apis and requirements, see https://github.com/FINTLabs/fint-kafka#readme.

## Dependency

To include this library in your Gradle project, add the following dependency:

```gradle
dependencies {
    implementation 'no.fintlabs:fint-flyt-kafka:<version>'
}
```

Replace `<version>` with the latest release version.

## Comparison: fint-kafka vs fint-flyt-kafka

| Feature                         | fint-kafka                          | fint-flyt-kafka                      |
|---------------------------------|-----------------------------------|------------------------------------|
| Basic Kafka producer/consumer   | ✔                                 | ✔                                  |
| Application property configuration | ✘                               | ✔                                  |
| Instance flow headers support   | ✘                                 | ✔                                  |
| Specialized producer factories  | ✘                                 | ✔                                  |
| Specialized consumer factories  | ✘                                 | ✔                                  |
| Error event handling            | ✘                                 | ✔                                  |

## Configuration

To enable FINT Flyt Kafka configuration in your Spring Boot application, add the following to your main application class:

```java
import no.fintlabs.kafka.flyt.FlytKafkaConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(FlytKafkaConfiguration.class)
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

## Application properties

### Autoconfigured properties
To import these configurations, add ``@Import(FlytKafkaConfiguration.class)`` to your main application.
* ``fint.kafka.topic.domain-context: flyt`` - Used as the second component in generated topic names

## Instance flow API

### Producers
Following are the instance flow producer factories and producers:
* ``InstanceFlowEntityProducerFactory`` -> ``InstanceFlowEntityProducer<T>``
* ``InstanceFlowEventProducerFactory`` -> ``InstanceFlowEventProducer<T>``
* ``InstanceFlowErrorEventProducer`` (autowired directly)

#### Examples
##### Entity
```java
@Autowired InstanceFlowEntityProducerFactory entityProducerFactory;

InstanceFlowEntityProducer<Object> producer = entityProducerFactory.createProducer(Object.class);

Object entity = new Object();
// send message
producer.send(entity);
```

##### Event
```java
@Autowired InstanceFlowEventProducerFactory eventProducerFactory;

InstanceFlowEventProducer<SakResource> producer = eventProducerFactory.createProducer(SakResource.class);

SakResource event = new SakResource();
// send event
producer.send(event);
```

##### Error event
```java
@Autowired InstanceFlowErrorEventProducer errorEventProducer;

ErrorEvent errorEvent = new ErrorEvent();
// send error event
errorEventProducer.send(errorEvent);
```

### Consumers
Instance flow consumers are created with the ``InstanceFlowEntityConsumerFactoryService``, ``InstanceFlowEventConsumerFactoryService``, ``InstanceFlowErrorEventConsumerFactoryService`` and ``InstanceFlowRequestConsumerFactoryService``. These factory services create an instance of ``ListenerContainerFactory<VALUE, TOPIC_NAME_PARAMETERS, TOPIC_NAME_PATTERN_PARAMETERS>`` which is configured to accept only the appropriate topic name parameters for the respective kafka message pattern. The listener container is then created by passing either topic name parameters or topic name pattern parameters to the ``ListenerContainerFactory``.

**NB:** The ``ConcurrentMessageListenerContainer<K, V>`` that is created by the ``ListenerContainerFactory`` has to be registered in the Spring context to be picked up by the Apache Kafka library. This is usually done by creating and returning the consumer in a @Bean annotated method in a configuration class.

A simple ``InstanceFlowErrorHandler`` is a ``CommonErrorHandler`` implementation that logs and rethrows errors. You may also provide your own implementation.

#### Examples
##### Entity
```java
@Autowired InstanceFlowEntityConsumerFactoryService entityConsumerFactoryService;

ListenerContainerFactory<AdministrativEnhetResource, EntityTopicNameParameters, EntityTopicNamePatternParameters> factory =
    entityConsumerFactoryService.createFactory(
            AdministrativEnhetResource.class,
            consumerRecord -> processInstanceFlowEntity(consumerRecord.value()),
            new InstanceFlowErrorHandler()
    );

ConcurrentMessageListenerContainer<?, ?> container = factory.createContainer(
        EntityTopicNameParameters
            .builder()
            .resource("arkiv.noark.administrativenhet")
            .build()
);

// Register container as a Spring bean or start it manually

// The processInstanceFlowEntity method would be defined to handle the consumed entity
```

##### Event
```java
@Autowired InstanceFlowEventConsumerFactoryService eventConsumerFactoryService;

ListenerContainerFactory<Instance, EventTopicNameParameters, EventTopicNamePatternParameters> factory =
    eventConsumerFactoryService.createFactory(
            Instance.class,
            consumerRecord -> processInstanceFlowEvent(consumerRecord.value()),
            new InstanceFlowErrorHandler()
    );

ConcurrentMessageListenerContainer<?, ?> container = factory.createContainer(
        EventTopicNameParameters
            .builder()
            .eventName("adapter-health")
            .build()
);

// Register container as a Spring bean or start it manually

// The processInstanceFlowEvent method would be defined to handle the consumed event
```

##### Error event
```java
@Autowired InstanceFlowErrorEventConsumerFactoryService errorEventConsumerFactoryService;

ListenerContainerFactory<Object, ErrorEventTopicNameParameters, ErrorEventTopicNamePatternParameters> factory =
    errorEventConsumerFactoryService.createFactory(
            consumerRecord -> processInstanceFlowErrorEvent(consumerRecord.value()),
            new InstanceFlowErrorHandler()
    );

ConcurrentMessageListenerContainer<?, ?> container = factory.createContainer(
        ErrorEventTopicNameParameters
            .builder()
            .errorEventName("instance-processing-error")
            .build()
);

// Register container as a Spring bean or start it manually

// The processInstanceFlowErrorEvent method would be defined to handle the consumed error event
```

##### Request
```java
@Autowired InstanceFlowRequestConsumerFactoryService requestConsumerFactoryService;

ListenerContainerFactory<String, RequestTopicNameParameters, RequestTopicNamePatternParameters> factory =
    requestConsumerFactoryService.createFactory(
            String.class,
            SakResource.class,
            consumerRecord -> createInstanceFlowReplyProducerRecord(consumerRecord),
            new InstanceFlowErrorHandler()
    );

ConcurrentMessageListenerContainer<?, ?> container = factory.createContainer(
        RequestTopicNameParameters
            .builder()
            .resource("arkiv.noark.sak")
            .parameterName("mappeid")
            .build()
);

// Register container as a Spring bean or start it manually

// The createInstanceFlowReplyProducerRecord method would be defined to create a reply producer record for the request
```
