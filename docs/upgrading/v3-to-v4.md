# Oppgradering: `v3` -> `v4`

> Eksakte tagger for dette hoppet: `v3.1.2 -> v4.0.0`.

Dette hoppet er den største migreringen i bibliotekets historie. Det er samtidig et package-skifte, dependency-skifte og API-skifte.

Mange av endringene i dette hoppet kommer direkte fra overgangen i basebiblioteket. Les derfor denne guiden sammen med `../fint-kafka/docs/upgrading/v3-to-v4.md`. Nedenfor omtales bare det som er spesielt for `fint-flyt-kafka`.

## 1. Scope

- Fra major: `v3`
- Til major: `v4`
- Hopper over patch-detaljer i overskrifter, men guiden er skrevet mot `v3.1.2 -> v4.0.0`

## 2. Hurtigoversikt

| Område               | Endring                                                                                   | Påvirkning                                                |
|----------------------|-------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| Dependency/plattform | Følger i stor grad basebibliotekets migrering                                             | Se `../fint-kafka/docs/upgrading/v3-to-v4.md`             |
| Producer-API         | Spesialiserte entity/event/error-producere erstattes av `InstanceFlowTemplate`            | Kode må skrives om                                        |
| Consumer-API         | Spesialiserte consumer factory services erstattes av `InstanceFlowListenerFactoryService` | Kode må skrives om                                        |
| Request/reply        | Request-producer-wrapper fjernes                                                          | Bruk må fjernes eller flyttes til underliggende bibliotek |
| Feilhåndtering       | `InstanceFlowErrorHandler` fjernes til fordel for factory + konfig                        | Egen feilhåndtering må omskrives                          |

## 3. Basebibliotek og konfigurasjon

For generelle endringer i:

- dependency-koordinater
- Java-versjon
- `fint.kafka.*` vs `novari.kafka.*`
- topic-navngivning og topic-parameterobjekter

se `../fint-kafka/docs/upgrading/v3-to-v4.md`.

Det Flyt-spesifikke poenget er bare at samme konfigurasjonsmigrering også gjelder denne repoen. Applikasjoner som oppgraderer `fint-flyt-kafka` fra `v3` til `v4`, må derfor normalt oppdatere `application.yml` i tråd med guiden i `fint-kafka`.

## 4. Flyt-spesifikke API-endringer

### 4.1 Pakker og imports

Hovedregelen er:

- `no.fintlabs...` -> `no.novari...`
- instance flow-klasser flyttes ned i `instanceflow.*`-pakker

Eksempler:

| `v3`                                                              | `v4`                                                                      |
|-------------------------------------------------------------------|---------------------------------------------------------------------------|
| `no.fintlabs.flyt.kafka.InstanceFlowConsumerRecord`               | `no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord`  |
| `no.fintlabs.flyt.kafka.headers.InstanceFlowHeaders`              | `no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders`           |
| `no.fintlabs.flyt.kafka.entity.InstanceFlowEntityProducerFactory` | `no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory` |

### 4.2 Producer-API: spesialisert -> generisk

`v3` hadde ulike wrapper-klasser for entity, event og error-event:

- `InstanceFlowEntityProducerFactory`
- `InstanceFlowEntityProducer`
- `InstanceFlowEntityProducerRecord`
- `InstanceFlowEventProducerFactory`
- `InstanceFlowEventProducer`
- `InstanceFlowEventProducerRecord`
- `InstanceFlowErrorEventProducer`
- `InstanceFlowErrorEventProducerRecord`

I `v4` erstattes dette av:

- `InstanceFlowTemplateFactory`
- `InstanceFlowTemplate<V>`
- `InstanceFlowProducerRecord<V>`

Hva du sender avgjøres ikke lenger av producer-klassen, men av `TopicNameParameters` du legger i recorden.

### 4.3 Consumer-API: spesialiserte services -> en felles service

`v3`:

- `InstanceFlowEntityConsumerFactoryService`
- `InstanceFlowEventConsumerFactoryService`
- `InstanceFlowErrorEventConsumerFactoryService`

`v4`:

- `InstanceFlowListenerFactoryService`

Metoder:

| `v3`                       | `v4`                                        |
|----------------------------|---------------------------------------------|
| `createRecordFactory(...)` | `createRecordListenerContainerFactory(...)` |
| `createBatchFactory(...)`  | `createBatchListenerContainerFactory(...)`  |

### 4.4 Topic-parameterobjekter

De synlige endringene i topic-parameterobjektene kommer fra `fint-kafka`. Se `../fint-kafka/docs/upgrading/v3-to-v4.md` for bakgrunn.

Mest synlig i applikasjonskode:

- `resource(...)` blir `resourceName(...)`
- `orgId` og `domainContext` flyttes inn i `TopicNamePrefixParameters`

`v3`:

```java
EntityTopicNameParameters.builder()
        .resource("resource")
        .build();
```

`v4`:

```java
EntityTopicNameParameters.builder()
        .topicNamePrefixParameters(
                TopicNamePrefixParameters.stepBuilder()
                        .orgId("my-org")
                        .domainContext("flyt")
                        .build()
        )
        .resourceName("resource")
        .build();
```

### 4.5 Request/reply er delvis fjernet

Disse klassene finnes i `v3`, men ikke i `v4`:

- `InstanceFlowRequestProducerFactory`
- `InstanceFlowRequestProducer`
- `InstanceFlowRequestProducerRecord`

Hvis du brukte request-producer-wrapperen i `v3`, finnes det ingen direkte erstatning i `v4` i denne repoen.

Request-consumer-wrapperen finnes fortsatt i `v4`, men API-et er endret:

| `v3`                                                                 | `v4`                                                                                            |
|----------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `createRecordFactory(...)` som returnerer `ListenerContainerFactory` | `createRecordConsumerContainer(...)` som returnerer ferdig `ConcurrentMessageListenerContainer` |

### 4.6 Feilhandtering er redesignet

`v3` hadde en abstrakt `InstanceFlowErrorHandler` som arvet `DefaultErrorHandler`.

`v4` fjerner denne og introduserer:

- `InstanceFlowErrorHandlerFactory`
- `InstanceFlowErrorHandlerConfiguration<V>`

Det betyr at custom feilhåndtering ikke lenger lages ved å subklasse `DefaultErrorHandler`, men ved å bygge konfig og la factory opprette handleren.

### 4.7 Returtype fra producer-send

`v3` wrapperne returnerte `ListenableFuture<SendResult<...>>`.

`v4` `InstanceFlowTemplate.send(...)` returnerer `CompletableFuture<SendResult<...>>`.

## 5. Kodeeksempler (gammel -> ny)

### 5.1 Event-producer

`v3`:

```java
InstanceFlowEventProducer<MyEvent> producer =
        instanceFlowEventProducerFactory.createProducer(MyEvent.class);

producer.send(
        InstanceFlowEventProducerRecord.<MyEvent>builder()
                .topicNameParameters(
                        EventTopicNameParameters.builder()
                                .eventName("adapter-health")
                                .build()
                )
                .instanceFlowHeaders(headers)
                .value(event)
                .build()
);
```

`v4`:

```java
InstanceFlowTemplate<MyEvent> template =
        instanceFlowTemplateFactory.createTemplate(MyEvent.class);

template.send(
        InstanceFlowProducerRecord.<MyEvent>builder()
                .topicNameParameters(
                        EventTopicNameParameters.builder()
                                .topicNamePrefixParameters(
                                        TopicNamePrefixParameters.stepBuilder()
                                                .orgId("my-org")
                                                .domainContext("flyt")
                                                .build()
                                )
                                .eventName("adapter-health")
                                .build()
                )
                .instanceFlowHeaders(headers)
                .value(event)
                .build()
);
```

### 5.2 Entity-consumer

`v3`:

```java
instanceFlowEntityConsumerFactoryService.createRecordFactory(
        MyEntity.class,
        record -> handle(record.getConsumerRecord().value())
).createContainer(
        EntityTopicNameParameters.builder()
                .resource("resource")
                .build()
);
```

`v4`:

```java
instanceFlowListenerFactoryService.createRecordListenerContainerFactory(
        MyEntity.class,
        record -> handle(record.getConsumerRecord().value()),
        ListenerConfiguration.stepBuilder()
                .groupIdApplicationDefault()
                .maxPollRecordsKafkaDefault()
                .maxPollIntervalKafkaDefault()
                .continueFromPreviousOffsetOnAssignment()
                .build(),
        null
).createContainer(
        EntityTopicNameParameters.builder()
                .topicNamePrefixParameters(
                        TopicNamePrefixParameters.stepBuilder()
                                .orgId("my-org")
                                .domainContext("flyt")
                                .build()
                )
                .resourceName("resource")
                .build()
);
```

## 6. Endret oppførsel

- Biblioteket er ikke lenger organisert rundt message-type-spesifikke wrapper-klasser.
- Request-producer-wrapperen er borte allerede i `v4`.
- Topic-navn forventer den nyere modellen fra `no.novari:kafka`; se baseguiden for detaljer.
- Bytte til Java `21` følger av basebiblioteket; se `../fint-kafka/docs/upgrading/v3-to-v4.md`.

## 7. Sjekkliste

1. Oppdater dependency fra `no.fintlabs:fint-flyt-kafka` til `no.novari:flyt-kafka`.
2. Folg konfigurasjons- og plattformguiden i `../fint-kafka/docs/upgrading/v3-to-v4.md`.
3. Oppdater JDK til `21`.
4. Bytt imports fra `no.fintlabs...` til `no.novari...`.
5. Skriv om producers til `InstanceFlowTemplateFactory` og `InstanceFlowProducerRecord`.
6. Skriv om consumer-oppsett til `InstanceFlowListenerFactoryService`.
7. Erstatt bruk av `InstanceFlowErrorHandler` med `InstanceFlowErrorHandlerFactory` og konfig-builder.
8. Fjern eller skriv om bruk av request-producer-wrapperen; den finnes ikke lenger i `v4`.
