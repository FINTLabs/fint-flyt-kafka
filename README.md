# fint-flyt-kafka

[![CI](https://github.com/FINTLabs/fint-flyt-kafka/actions/workflows/ci.yaml/badge.svg)](https://github.com/FINTLabs/fint-flyt-kafka/actions/workflows/ci.yaml)

Flyt additions to [fint-kafka-starter](https://github.com/FINTLabs/fint-kafka-starter) (`no.novari:kafka-starter`):

- `InstanceFlowHeaders`, sent as JSON in the Kafka header `flyt.instance-flow-headers`. Create it with the constructor and
  change it with `copy(...)`
- `KafkaTemplate.send(topic, instanceFlowHeaders, value)` that writes the header
- a converter so a `@KafkaListener` can take the header as a parameter
- the error event payloads `InstanceErrorEvent`, `ErrorCollection` and `Error`

Topics, consumer groups, producers, consumers and error handling are standard Spring Kafka with fint-kafka-starter. See
its README and [migration guide](https://github.com/FINTLabs/fint-kafka-starter/blob/main/docs/migrating-from-fint-kafka.md).

## Dependency

```kotlin
implementation("no.novari:flyt-kafka:<version>")
```

Requires Spring Boot 4.1+, Jackson 3 and Java 21+. The library is auto-configured and brings fint-kafka-starter. Upgrading
from v7: [docs/upgrading/v7-to-v8.md](docs/upgrading/v7-to-v8.md).

Flyt apps set `novari.kafka.topic.domain-context: flyt`, or include the profile `flyt-kafka`, which sets it.

## Producing

```kotlin
@Service
class InstanceMappedEventProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val instanceMappedTopic: EventTopicName,
) {
    fun publish(instanceFlowHeaders: InstanceFlowHeaders, mappedInstance: Any) {
        kafkaTemplate.send(instanceMappedTopic, instanceFlowHeaders, mappedInstance)
    }
}
```

`send` is in `no.novari.flyt.kafka.instanceflow.producing`. It also takes `key` and `additionalHeaders`, for example
`flyt.actor`. A `null` value is a tombstone.

## Consuming

```kotlin
@Component
class InstanceRegisteredEventListener(
    private val processingService: InstanceProcessingService,
) {
    @KafkaListener(topics = ["#{@instanceRegisteredTopic.name}"])
    fun on(
        instance: InstanceObject,
        @Header(INSTANCE_FLOW_HEADERS_KEY) headers: InstanceFlowHeaders,
    ) = processingService.process(instance, headers)
}
```

- A missing header fails the record with Spring's `MessageHandlingException`, an unreadable header with
  `CouldNotReadInstanceFlowHeadersException` as the cause. Both are retried by the app's error handler, as before.
- Topics with only `null` values (`instance-dispatched`, `instance-deleted`): leave out the value parameter, or take it
  with its type and `@Payload(required = false)`. Do not use `Any?`: it gets the `ConsumerRecord` for a tombstone.
- In an error handler or a container made in code, read the header from the record with
  `record.headers().instanceFlowHeaders()`, or `instanceFlowHeadersOrNull()` to get `null` when it is missing or cannot
  be read. The value is the raw JSON `String` there.

```kotlin
@Bean
fun kafkaErrorHandler(errorEvents: InstanceErrorEventProducerService): CommonErrorHandler =
    DefaultErrorHandler(
        { record, exception ->
            record.headers().instanceFlowHeadersOrNull()
                ?.let(errorEvents::publishGeneralSystemErrorEvent)
                ?: log.warn(exception) { "Missing instance flow headers, skipping" }
        },
        FixedBackOff(0, 0),
    )
```

## Wire compatibility tests

While apps move from v7 to v8, both versions must read each other's records. `wire-compat/fixtures/<version>` holds the
records each version produces, `ProduceFixturesTest` checks this version against its own, `NewReadsFixturesTest` reads
all fixtures with this version, and the `compat-v7` subproject reads them with fint-flyt-kafka 7.4.1. Fixtures of a
released version are never rewritten. Write the current version's fixtures with
`./gradlew test -PwriteWireCompatFixtures`. Delete `wire-compat` and `compat-v7` when no app runs v7.

## Locked API

The public API is checked for binary compatibility, because fint-flyt-gateway-starter is compiled against one version
and apps may pull in a newer one. A change to the API fails `./gradlew check` until `./gradlew apiDump` updates
`api/flyt-kafka.api`. Breaking changes, such as a new field in `InstanceFlowHeaders`, need a new major version.

## Dependencies

All versions are in `gradle/libs.versions.toml`, also those `compat-v7` uses. Resolved versions are locked in the
committed `gradle.lockfile`. After a manual dependency change, run `./gradlew dependencies --write-locks` and commit
the lockfile. Dependabot updates it in its own PRs. `compat-v7` is not locked, because Dependabot only rewrites the root
lockfile.
