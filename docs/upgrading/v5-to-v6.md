# Oppgradering: `v5` -> `v6`

> Eksakte tagger for dette hoppet: `v5.0.0 -> v6.0.0`.

Dette hoppet er primært et plattformhopp. Det er ingen endringer i bibliotekets public API i `src/main/java` mellom disse to taggene.

Det er derfor heller ikke behov for mye Flyt-spesifikk forklaring her. Hvis du trenger generell bakgrunn om underliggende Kafka-begreper, se `../fint-kafka/README.md`.

## 1. Scope

- Fra major: `v5`
- Til major: `v6`
- Guiden dekker hoppet `v5.0.0 -> v6.0.0`

## 2. Hurtigoversikt

| Område                        | Endring                                                                          | Påvirkning                                                 |
|-------------------------------|----------------------------------------------------------------------------------|------------------------------------------------------------|
| Dependency/plattform          | Java `21` -> `25`                                                                | Obligatorisk toolchain-oppgradering                        |
| Byggverktoy                   | Spring Boot Gradle plugin `3.5.7` -> `3.5.10`, Gradle wrapper `9.1.0` -> `9.3.1` | Oppdater CI og lokal build hvis du bygger biblioteket selv |
| Underliggende Kafka-bibliotek | Uendret: `no.novari:kafka:6.0.0`                                                 | Ingen ny basebibliotek-migrering her                       |
| API                           | Ingen endringer i `src/main/java`                                                | Ingen kodeendringer forventet i konsumerende applikasjoner |
| Konfigurasjon                 | Ingen observerte endringer                                                       | `application.yml` kan normalt beholdes som den er          |

## 3. Konfigurasjonsendringer

Det er ingen dokumenterbare endringer i bibliotekets brukerrettede konfigurasjon mellom `v5.0.0` og `v6.0.0`.

## 4. API-endringer

Det er ingen endringer i public source-API i denne repoen mellom taggene:

- samme pakker
- samme klasser
- samme offentlige metoder

Det betyr at migreringen i praksis handler om å kunne konsumere en artefakt bygget for nyere Java.

## 5. Viktigste konsekvens: Java `25`

`v6.0.0` er konfigurert med:

```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
```

Konsekvens for konsumerende applikasjoner:

- hvis applikasjonen din laster `v6`-artefakten, må runtime og build-kjede støtte Java `25`
- applikasjoner som fortsatt kjører på Java `21`, kan ikke uten videre oppgradere til `v6`

## 6. Byggrelaterte endringer

Mellom taggene endres blant annet:

- Spring Boot Gradle plugin: `3.5.7` -> `3.5.10`
- Gradle wrapper: `9.1.0` -> `9.3.1`
- Mockito i test: `5.12.0` -> `5.21.0`

Dette er ikke brukerrettede API-endringer, men de er relevante hvis:

- du bygger biblioteket selv
- du bruker samme build-oppsett som referanse
- CI-miljøet ditt har hardkodet eldre Java- eller Gradle-versjoner

## 7. Sjekkliste

1. Oppdater JDK i lokalutvikling og CI til `25`.
2. Verifiser at build-miljøet støtter Gradle `9.3.1` dersom du bygger biblioteket selv.
3. Behold eksisterende kode og konfigurasjon som utgangspunkt; det er ingen forventede API-endringer i applikasjonskoden.
4. Kjør applikasjonens integrasjons- og runtime-tester for å fange eventuelle Java- eller plattformrelaterte bivirkninger.
