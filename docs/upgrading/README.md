# Oppgraderingsguider (major versjoner)

Denne mappen inneholder oppgraderingsguider per major-hopp.

## Struktur

- En fil per hopp: `vX-to-vY.md`
- En fast mal for nye hopp: [`_TEMPLATE.md`](_TEMPLATE.md)
- Denne filen er indeksen

## Tilgjengelige guider

| Fra | Til | Guide |
|---|---|---|
| `v3` | `v4` | [`v3-to-v4.md`](v3-to-v4.md) |
| `v4` | `v5` | [`v4-to-v5.md`](v4-to-v5.md) |
| `v5` | `v6` | [`v5-to-v6.md`](v5-to-v6.md) |

Fotnoter (eksakte tagger):

- `v3 -> v4`: `v3.1.2 -> v4.0.0`
- `v4 -> v5`: `v4.0.0 -> v5.0.0`
- `v5 -> v6`: `v5.0.0 -> v6.0.0`

## Hvordan legge til ny major-guide (v7, v8, ...)

1. Kopier [`_TEMPLATE.md`](_TEMPLATE.md) til `v<forrige-major>-to-v<ny-major>.md`.
2. Fyll ut dokumentet basert på kode-diff mellom relevante tagger.
3. Oppdater tabellen i denne indeksen med ny rad.
4. Oppdater lenken i hoved-README hvis nødvendig.

## Anbefalt arbeidsmåte for hver ny guide

1. Dokumenter dependency- og plattformendringer.
2. Dokumenter konfig-endringer (`application.yml`/env).
3. Dokumenter API-endringer (pakker, klasser, metoder og record-/builder-felter).
4. Dokumenter endret oppførsel eller fjernet funksjonalitet.
5. Legg inn gamle vs nye kodeeksempler der migrering ikke er triviell.
6. Avslutt med en kort sjekkliste.
