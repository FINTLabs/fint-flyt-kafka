# FINT Flyt Kafka Project
[![CI](https://github.com/FINTLabs/fint-skjema-kafka/actions/workflows/ci.yaml/badge.svg)](https://github.com/FINTLabs/fint-skjema-kafka/actions/workflows/ci.yaml)

This library expands on the FINT Kafka library (fint-kafka) by configuring application properties and adding an api for producing and consuming kafka messages with instance flow headers. This readme only covers the added functionality. For information on the basic apis and requirements, see https://github.com/FINTLabs/fint-kafka#readme.

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
* ``InstanceFlowRequestProducerFactory`` -> ``InstanceFlowRequestProducer<V, R>``

#### Examples
##### Entity
```java
@Autowired InstanceFlowEntityProducerFactory entityProducerFactory;

entityProducerFactory.createProducer(Object.class);
```

##### Event
```java
@Autowired InstanceFlowEventProducerFactory eventProducerFactory;

eventProducerFactory.createProducer(SakResource.class)
```

##### Error event
```java
@Autowired InstanceFlowErrorEventProducer errorEventProducer;
```

##### Request
```java
@Autowired InstanceFlowRequestProducerFactory requestProducerFactory;

requestProducerFactory.createProducer(
                ReplyTopicNameParameters.builder().applicationId(applicationId).resource("arkiv.noark.sak").build(),
                String.class,
                SakResource.class
);
```


### Consumers
Instance flow consumers are created with the ``InstanceFlowEntityConsumerFactoryService``, ``InstanceFlowEventConsumerFactoryService``, ``InstanceFlowErrorEventConsumerFactoryService`` and ``InstanceFlowRequestConsumerFactoryService``. These factory services create an instance of ``ListenerContainerFactory<VALUE, TOPIC_NAME_PARAMETERS, TOPIC_NAME_PATTERN_PARAMETERS>`` which is configured to accept only the appropriate topic name parameters for the respective kafka message pattern. The listener container is then created by passing either topic name parameters or topic name pattern parameters to the ``ListenerContainerFactory``.

**NB:** The ``ConcurrentMessageListenerContainer<K, V>`` that is created by the ``ListenerContainerFactory`` has to be registered in the Spring context to be picked up by the Apache Kafka library. This is usually done by creating and returning the consumer in a @Bean annotated method in a configuration class, but can also be done through the ``ListenerBeanRegistrationService``.

#### Examples
##### Entity
```java
@Autowired InstanceFlowEntityConsumerFactoryService entityConsumerFactoryService;

entityConsumerFactoryService.createFactory(
        AdministrativEnhetResource.class,
        consumerRecord -> processInstanceFlowEntity(consumerRecord.value()),
        new InstanceFlowErrorHandler()
).createContainer(
        EntityTopicNameParameters
            .builder()
            .resource("arkiv.noark.administrativenhet")
            .build()
);

```
##### Event
```java
@Autowired InstanceFlowEventConsumerFactoryService eventConsumerFactoryService;

eventConsumerFactoryService.createFactory(
        Instance.class,
        consumerRecord -> processInstanceFlowEvent(consumerRecord.value()),
        new InstanceFlowErrorHandler()
).createContainer(
        EventTopicNameParameters
            .builder()
            .eventName("adapter-health")
            .build()
);
```

##### Error event
```java
@Autowired InstanceFlowErrorEventConsumerFactoryService errorEventConsumerFactoryService;

errorEventConsumerFactoryService.createFactory(
        consumerRecord -> processInstanceFlowErrorEvent(consumerRecord.value()),
        new InstanceFlowErrorHandler()
).createContainer(
        ErrorEventTopicNameParameters
            .builder()
            .errorEventName("instance-processing-error")
            .build()
);
```

##### Request
```java
@Autowired InstanceFlowRequestConsumerFactoryService requestConsumerFactoryService;

requestConsumerFactoryService.createFactory(
        String.class,
        SakResource.class,
        (consumerRecord) -> createInstanceFlowReplyProducerRecord(consumerRecord),
        new InstanceFlowErrorHandler()
).createContainer(
        RequestTopicNameParameters
            .builder()
            .resource("arkiv.noark.sak")
            .parameterName("mappeid")
            .build()
        );
```