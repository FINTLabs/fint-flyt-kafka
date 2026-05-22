## Oppgradering: `v6` -> `v7`

> Eksakte tagger for dette hoppet: `v6.0.0 -> v7.0.0`.

`v7` er en full migrering av biblioteket fra Java til Kotlin. JSON-formatet på Kafka-meldinger og header-nøkkelen `flyt.instance-flow-headers` er uendret, så `v6`- og `v7`-applikasjoner kan utveksle meldinger uten koordinert rollout.

## 1. Scope

- Fra major: `v6`
- Til major: `v7`
- Guiden dekker hoppet `v6.0.0 -> v7.0.0`

## 2. Hurtigoversikt

| Område                        | Endring                                                                                                                                                  | Påvirkning                                                                              |
|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| Språk                         | Java -> Kotlin (`2.3.10`)                                                                                                                                | Bibliotekets kildekode er omskrevet, men offentlig API er stort sett kildekode-kompatibel for både Java- og Kotlin-brukere |
| Dependency/plattform          | `kotlin-reflect` og `jackson-module-kotlin` er nye `api`-avhengigheter                                                                                   | Spring Boot 3.x auto-konfigurerer Jackson Kotlin-modulen for Kotlin data classes; for Java-brukere er dette transitivt og krever ingen handling |
| Byggverktoy                   | Gradle wrapper `9.3.1` -> `9.1.0` (siden Kotlin Gradle Plugin `2.3.10` ikke spiller pent med Gradle `9.3.x`)                                              | Oppdater CI og lokal build hvis du bygger biblioteket selv                              |
| Underliggende Kafka-bibliotek | Uendret: `no.novari:kafka:6.0.0`                                                                                                                         | Ingen ny basebibliotek-migrering her                                                    |
| API – wire-format             | JSON-format for `InstanceFlowHeaders`, `Error` og `ErrorCollection` er uendret                                                                          | `v6`- og `v7`-applikasjoner kan utveksle meldinger uten koordinert rollout              |
| API – `Optional`              | `Optional<X>`-getters er erstattet med Kotlin nullable (`X?`)                                                                                            | Java-brukere må endre `getDefaultBackoff().ifPresent(...)`-style kode til null-sjekker  |
| API – `InstanceFlowRecoverer` | `BiFunction<..., Optional<BackOff>>` er endret til `BiFunction<..., BackOff?>` på `retryWithBackoffFunction`                                              | Custom backoff-funksjoner må returnere nullable i stedet for `Optional`                  |
| Konfigurasjon                 | Ingen observerte endringer                                                                                                                               | `application.yml` kan normalt beholdes som den er                                       |

## 3. Konfigurasjonsendringer

Ingen endringer i bibliotekets brukerrettede konfigurasjon mellom `v6.0.0` og `v7.0.0`. Header-nøkkelen `flyt.instance-flow-headers` er uendret.

## 4. API-endringer

### 4.1 Klasser og services

Alle klasser ligger i samme pakker som før:

- `no.novari.flyt.kafka.model.Error`
- `no.novari.flyt.kafka.model.ErrorCollection`
- `no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders`
- `no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeadersMapper`
- `no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecord`
- `no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowConsumerRecordMapper`
- `no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowErrorHandlerConfiguration`
- `no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowErrorHandlerConfigurationStepBuilder`
- `no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowErrorHandlerFactory`
- `no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService`
- `no.novari.flyt.kafka.instanceflow.producing.InstanceFlowProducerRecord`
- `no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplate`
- `no.novari.flyt.kafka.instanceflow.producing.InstanceFlowTemplateFactory`

### 4.2 Builder-API

Alle eksisterende builder-API-er er bevart med samme signaturer:

- `InstanceFlowHeaders.builder()...build()`
- `InstanceFlowHeaders.toBuilder()...build()`
- `InstanceFlowProducerRecord.builder<V>()...build()`
- `InstanceFlowConsumerRecord.builder<T>()...build()`
- `Error.builder()...build()`
- `InstanceFlowErrorHandlerConfiguration.stepBuilder<V>()...build()`

Step-builderen for `InstanceFlowErrorHandlerConfiguration` har identisk interface-struktur og samme steg-rekkefølge som i `v6`.

### 4.3 `Optional` -> nullable

Følgende getters har endret returtype:

| Klasse                                                   | Gammel (`v6`)                                                                                  | Ny (`v7`)                                                                                       |
|----------------------------------------------------------|------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `InstanceFlowErrorHandlerConfiguration.getBackOffFunction()` | `Optional<BiFunction<InstanceFlowConsumerRecord<V>, Exception, Optional<BackOff>>>`            | property `backOffFunction: BiFunction<InstanceFlowConsumerRecord<V>, Exception, BackOff?>?`     |
| `InstanceFlowErrorHandlerConfiguration.getDefaultBackoff()` | `Optional<BackOff>`                                                                            | property `defaultBackoff: BackOff?`                                                             |
| `InstanceFlowErrorHandlerConfiguration.getCustomRecoverer()` | `Optional<InstanceFlowRecoverer<V>>`                                                           | property `customRecoverer: InstanceFlowRecoverer<V>?`                                           |

### 4.4 `retryWithBackoffFunction` returnerer nullable

Signaturen til funksjonen du sender til `retryWithBackoffFunction(...)` er endret:

```java
// v6
.retryWithBackoffFunction(
    (record, exception) -> Optional.of(new FixedBackOff(1000L, 3L))
)

// v7
.retryWithBackoffFunction(
    (record, exception) -> new FixedBackOff(1000L, 3L)  // returner null hvis du vil bruke default
)
```

### 4.5 Kodeeksempler (gammel -> ny)

Direkte tilgang på `Optional` (kun relevant for Java-brukere):

```java
// v6 (Java)
config.getDefaultBackoff().ifPresent(this::useBackoff);
BackOff backoff = config.getDefaultBackoff().orElse(null);

// v7 (Java)
if (config.getDefaultBackoff() != null) {
    useBackoff(config.getDefaultBackoff());
}
// eller:
BackOff backoff = config.getDefaultBackoff();
```

Kotlin-brukere skriver naturlig:

```kotlin
// v7 (Kotlin)
config.defaultBackoff?.let(::useBackoff)
val backoff: BackOff? = config.defaultBackoff
```

Builder-API-et er uendret:

```java
// fungerer både i v6 og v7
InstanceFlowHeaders headers = InstanceFlowHeaders.builder()
        .sourceApplicationId(10L)
        .correlationId(correlationId)
        .build();
```

## 5. Endret oppførsel

- JSON-format for `InstanceFlowHeaders` og `Error`/`ErrorCollection`: uendret bytewise. Felt-navn, defaults og header-nøkkel er bevart.
- `Builder.build()` på `InstanceFlowHeaders` kaster fortsatt `NullPointerException` hvis `sourceApplicationId` eller `correlationId` mangler. Meldingen er nesten identisk: `"sourceApplicationId is marked non-null but is null"` / `"correlationId is marked non-null but is null"`.
- Spring-beans registreres på samme måte som før via `@Service`-annoterte klasser.

## 6. Konsekvenser for Java-applikasjoner

Java-applikasjoner kan fortsatt bruke biblioteket. Kompatibilitetsbruddene er:

1. `Optional`-getters er fjernet — bytt til null-sjekker.
2. `BiFunction` til `retryWithBackoffFunction` returnerer `BackOff?` i stedet for `Optional<BackOff>`.

Builder-API-er, klassenavn, pakker og JSON-format er uendret, så det meste av Java-kode kompilerer som før.

## 7. Sjekkliste

1. Oppdater dependency til `no.novari:flyt-kafka:7.0.0`.
2. Erstatt `Optional.<X>`-bruk i applikasjonskoden (se 4.3 og 4.5).
3. Hvis du bruker `retryWithBackoffFunction(...)`, endre lambda til å returnere `BackOff?` (returner `null` i stedet for `Optional.empty()`).
4. Hvis du bygger biblioteket selv:
    - Påse at Gradle wrapper er `9.1.0` (Kotlin Gradle Plugin `2.3.10` har en kjent inkompatibilitet med Gradle `9.3.x`).
    - JDK-toolchain er fortsatt `25`.
5. Kjør applikasjonens integrasjons- og runtime-tester for å verifisere at meldinger fortsatt produseres/konsumeres som forventet.
