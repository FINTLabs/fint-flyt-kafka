# Upgrading: `v7` -> `v8`

v8 replaces fint-kafka 6 (`no.novari:kafka`) with fint-kafka-starter (`no.novari:kafka-starter`) and moves to Spring
Boot 4. All Kafka code in the app is rewritten to standard Spring Kafka. Follow fint-kafka-starter's
[migration guide](https://github.com/FINTLabs/fint-kafka-starter/blob/main/docs/migrating-from-fint-kafka.md) for
topics, group ids, error handling and properties. This guide covers the flyt parts.

v7 and v8 read each other's records, so apps can be upgraded one at a time and rolled back. The `wire-compat` tests
check this.

## Platform

- Spring Boot 4.1+, Jackson 3, Java 21+. The library is built with JDK 25 and published for Java 21.
- `no.novari:kafka` and request/reply are gone. Apps that use request/reply move to HTTP first (FFS-2389).
- The library is auto-configured. Remove `scanBasePackages = ["no.novari"]` or `@ComponentScan` of
  `no.novari.flyt.kafka`, unless the app needs it for other libraries.

## API

| v7                                                                             | v8                                                                                      |
|--------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| `InstanceFlowTemplateFactory.createTemplate(X::class.java)`                    | inject `KafkaTemplate<String, X>`                                                       |
| `InstanceFlowTemplate.send(InstanceFlowProducerRecord)`                        | `kafkaTemplate.send(topic, instanceFlowHeaders, value, key, additionalHeaders)`         |
| `InstanceFlowProducerRecord.additionalHeader(key, bytes)`                      | `additionalHeaders = mapOf(key to bytes)`                                               |
| `InstanceFlowListenerFactoryService.createRecordListenerContainerFactory(...)` | `@KafkaListener` with `@Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders` |
| `InstanceFlowConsumerRecord<T>`                                                | the value and the header as listener parameters                                         |
| `consumerRecord.headers()`, `timestamp()`, ...                                 | `@Header(...)`, `@Header(KafkaHeaders.RECEIVED_TIMESTAMP)`, ...                         |
| `InstanceFlowErrorHandlerFactory`, `InstanceFlowErrorHandlerConfiguration`     | a `CommonErrorHandler` bean, for example `DefaultErrorHandler`                          |
| `InstanceFlowRecoverer.recover(instanceFlowConsumerRecord, e)`                 | recoverer that calls `record.headers().instanceFlowHeaders()`                           |
| `recoverForMissingInstanceFlowHeaders(...)`                                    | `record.headers().instanceFlowHeadersOrNull()` gives `null`                             |
| `retryWithBackoffFunction { instanceFlowConsumerRecord, e -> }`                | `setBackOffFunction { record, e -> }`                                                   |
| `InstanceFlowHeadersMapper.getInstanceFlowHeaders(headers)`                    | `headers.instanceFlowHeaders()`                                                         |
| `InstanceFlowHeaders.builder()...build()`                                      | `InstanceFlowHeaders(sourceApplicationId = ..., correlationId = ...)`                   |
| `headers.toBuilder().instanceId(id).build()`                                   | `headers.copy(instanceId = id)`                                                         |
| `Error.builder()...build()`                                                    | `Error(errorCode = ..., args = ...)`                                                    |
| `*TopicNameParameters`                                                         | `KafkaTopics.event(...)`, `errorEvent(...)`, `entity(...)` as beans                     |

Unchanged: the fields of `InstanceFlowHeaders`, `Error`, `ErrorCollection` and `InstanceErrorEvent`, the header key
`flyt.instance-flow-headers`, `InstanceErrorOrigin`, the exceptions in `instanceflow.headers` and the JSON format. The
builders are gone: use the constructors, named arguments and `copy(...)`.

## Example

```kotlin
// v7
instanceFlowListenerFactoryService
    .createRecordListenerContainerFactory(
        InstanceObject::class.java,
        processingService::process,
        ListenerConfiguration.stepBuilder().groupIdApplicationDefault() /* ... */.build(),
        errorHandlerFactory.createErrorHandler(/* ... */),
    ).createContainer(EventTopicNameParameters.builder().eventName("instance-registered") /* ... */.build())

// v8
@KafkaListener(topics = ["#{@instanceRegisteredTopic.name}"])
fun on(
    instance: InstanceObject,
    @Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders,
) = processingService.process(instance, headers)
```

## Behaviour

- **Group ids:** a `@KafkaListener` without `id` and `groupId` uses `spring.kafka.consumer.group-id`, as
  `groupIdApplicationDefault()` did.
- **Missing or unreadable header:** the record fails with Spring's `MessageHandlingException`, or with
  `CouldNotReadInstanceFlowHeadersException` as the cause. It is retried by the error handler, as in v7.
- **Tombstones:** leave out the value parameter, or take it with its type and `@Payload(required = false)`. An `Any?`
  parameter gets the `ConsumerRecord` for a tombstone.
- **In a recoverer or back-off function**, `record.value()` is the raw JSON `String`.
- **JSON:** values are written with the app's Jackson 3 `JsonMapper`. `java.util.Date` is written as
  `2026-11-12T10:15:30.123Z` instead of `2026-11-12T10:15:30.123+00:00`. v7 reads both.
- **`InstanceFlowHeaders`** is written with the library's own `JsonMapper`, not the app's.

## Checklist

1. Spring Boot 4.1+, Jackson 3, Java 21+, no request/reply.
2. `no.novari:flyt-kafka:8.x`; remove `no.novari:kafka`.
3. Follow fint-kafka-starter's migration guide for topics, group ids, error handling and properties.
4. Rewrite producers and listeners as above.
5. Check group ids and offsets in staging (fint-kafka-starter's guide, "Check in staging").
