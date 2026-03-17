# Oppgradering: `v4` -> `v5`

> Eksakte tagger for dette hoppet: `v4.0.0 -> v5.0.0`.

Dette hoppet er mindre enn `v3 -> v4`, men fortsatt breaking. Det som forsvinner helt er request/reply-wrapperne, og det som endres mest er API-et for custom feilhåndtering.

Store deler av bakteppet her kommer fra at biblioteket gikk over til nyere `no.novari:kafka`. For generell bakgrunn om basebiblioteket, se `../fint-kafka/README.md` og `../fint-kafka/docs/upgrading/v4-to-v6.md`. Nedenfor er bare Flyt-spesifikke punkter tatt med.

## 1. Scope

- Fra major: `v4`
- Til major: `v5`
- Guiden dekker hoppet `v4.0.0 -> v5.0.0`

## 2. Hurtigoversikt

| Område               | Endring                                                          | Påvirkning                                    |
|----------------------|------------------------------------------------------------------|-----------------------------------------------|
| Dependency/plattform | Folger i stor grad basebibliotekets utvikling                    | Se `../fint-kafka/docs/upgrading/v4-to-v6.md` |
| Konfigurasjon        | Ingen egne property-renames observert i denne repoen             | Normalt lite arbeid her                       |
| Request/reply        | Resterende request/reply-wrappere fjernes                        | Kode må flyttes til `no.novari:kafka` direkte |
| Feilhåndtering       | `InstanceFlowErrorHandlerConfiguration` og builderen redesignes  | Custom error handling må skrives om           |
| Mapper-API           | `toFlytConsumerRecord(s)` blir `toInstanceFlowConsumerRecord(s)` | Bare relevant hvis du brukte mapperen direkte |

## 3. Basebibliotek og konfigurasjon

Det finnes ingen dokumenterbare property-endringer mellom `v4.0.0` og `v5.0.0` i `fint-flyt-kafka`-koden.

Hvis applikasjonen din også bruker `no.novari:kafka` direkte, bør du lese `../fint-kafka/docs/upgrading/v4-to-v6.md` for generell bakgrunn. Denne guiden beskriver bare det som faktisk ble fjernet eller renamed i Flyt-wrapperen.

## 4. API-endringer

### 4.1 Request/reply-wrappere er helt fjernet

Disse klassene finnes i `v4`, men ikke i `v5`:

- `no.novari.flyt.kafka.requestreply.InstanceFlowRequestConsumerFactoryService`
- `no.novari.flyt.kafka.requestreply.InstanceFlowReplyProducerRecord`

Konsekvens:

- request/reply må heretter bygges med `no.novari:kafka` direkte
- denne repoen wrapper ikke lenger request/reply for Flyt

### 4.2 Error handler-konfig er redesign

`v4`:

- `InstanceFlowErrorHandlerConfiguration.stepBuilder(Class<VALUE>)`
- `handleFailedRecords(...)`
- custom recoverer som `BiConsumer` eller `TriConsumer`

`v5`:

- `InstanceFlowErrorHandlerConfiguration.stepBuilder()`
- `recoverFailedRecords(InstanceFlowRecoverer<VALUE>)`
- custom recoverer blir et interface med to metoder:
  - `recover(...)`
  - `recoverForMissingInstanceFlowHeaders(...)`

Builderen i `v5` får også flere eksplisitte valg:

- retry-klassifisering: `retryOnly(...)`, `excludeExceptionsFromRetry(...)`, `useDefaultRetryClassification()`
- oppførsel ved exception-endring: `restartRetryOnExceptionChange()` eller `continueRetryOnExceptionChange()`
- oppførsel ved recovery-feil: `skipRecordOnRecoveryFailure()`, `reprocessAndRetryRecordOnRecoveryFailure()`, `reprocessRecordOnRecoveryFailure()`

### 4.3 Mapper-metoder er renamed

| `v4`                         | `v5`                                 |
|------------------------------|--------------------------------------|
| `toFlytConsumerRecord(...)`  | `toInstanceFlowConsumerRecord(...)`  |
| `toFlytConsumerRecords(...)` | `toInstanceFlowConsumerRecords(...)` |

Dette er normalt bare relevant hvis applikasjonen brukte `InstanceFlowConsumerRecordMapper` direkte.

## 5. Kodeeksempler (gammel -> ny)

### 5.1 Custom error handler-konfig

`v4`:

```java
DefaultErrorHandler errorHandler = instanceFlowErrorHandlerFactory.createErrorHandler(
        InstanceFlowErrorHandlerConfiguration.<MyEvent>stepBuilder(MyEvent.class)
                .retryWithFixedInterval(Duration.ofSeconds(10), 3)
                .handleFailedRecords((record, exception) -> {
                    log.error("Kunne ikke prosessere melding", exception);
                })
                .build()
);
```

`v5`:

```java
DefaultErrorHandler errorHandler = instanceFlowErrorHandlerFactory.createErrorHandler(
        InstanceFlowErrorHandlerConfiguration.<MyEvent>stepBuilder()
                .retryWithFixedInterval(Duration.ofSeconds(10), 3)
                .useDefaultRetryClassification()
                .restartRetryOnExceptionChange()
                .recoverFailedRecords(new InstanceFlowErrorHandlerConfiguration.InstanceFlowRecoverer<>() {
                    @Override
                    public void recover(InstanceFlowConsumerRecord<MyEvent> record, Exception exception) {
                        log.error("Kunne ikke prosessere melding", exception);
                    }

                    @Override
                    public void recoverForMissingInstanceFlowHeaders(
                            ConsumerRecord<String, MyEvent> consumerRecord,
                            Exception exception
                    ) {
                        log.error("Kunne ikke prosessere melding uten lesbare instance flow headers", exception);
                    }
                })
                .reprocessAndRetryRecordOnRecoveryFailure()
                .build()
);
```

### 5.2 Request/reply

`v4` hadde fortsatt en Flyt-wrapper for request-consumer:

```java
instanceFlowRequestConsumerFactoryService.createRecordConsumerContainer(
        requestTopicNameParameters,
        String.class,
        ReplyPayload.class,
        this::reply,
        listenerConfiguration,
        errorHandler
);
```

I `v5` finnes ingen tilsvarende klasse i denne repoen. Migreringen er derfor ikke en ren rename. Koden må flyttes til request/reply-API-et i `no.novari:kafka`; se `../fint-kafka/README.md` for dokumentasjon av det API-et.

## 6. Endret oppførsel

- `InstanceFlowErrorHandlerFactory` mapper nå konfig via `InstanceFlowErrorHandlerConfigurationMapper`.
- Custom recoverer kan kalles selv om instance flow headers mangler eller ikke kan leses, via egen fallback-metode.
- Request/reply er ikke lenger en del av dette bibliotekets ansvar.

## 7. Sjekkliste

1. Verifiser migrering til `no.novari:kafka:6.0.0` der applikasjonen bruker basebiblioteket direkte.
2. Fjern all bruk av `no.novari.flyt.kafka.requestreply.*`.
3. Skriv om error-handler-konfig til `stepBuilder()` uten klasseargument.
4. Erstatt `handleFailedRecords(...)` med `recoverFailedRecords(...)`.
5. Implementer `recoverForMissingInstanceFlowHeaders(...)` der du tidligere antok at headers alltid var til stede.
6. Oppdater eventuelle direkte kall til `InstanceFlowConsumerRecordMapper` med de nye metodenavnene.
