# fp-felles

Shared building blocks and shared external integration clients for Team
Foreldrepenger backend apps.

## Context

- [fp-context](https://github.com/navikt/fp-context) — team domain,
  architecture, conventions. Source of truth.
- Consumer view (which module to use when):
  [`architecture/team-libraries.md`](https://github.com/navikt/fp-context/blob/main/architecture/team-libraries.md).
- Copilot Space: navikt / **TeamForeldrepenger**.

## Two top-level modules

### `felles/` — building blocks

| Module | Purpose                                            |
|--------|----------------------------------------------------|
| `feil` | Structured exceptions and error codes              |
| `log` | Logback/SLF4J structured logging                   |
| `konfig` | App configuration                                  |
| `mapper` | Primary Jackson Json-mapping feature               |
| `kontekst` | Thread/request context                             |
| `oidc` | Incoming token validation + outgoing token provider/exchange |
| `abac`, `abac-kontekst` | XACML PEP/PDP framework                            |
| `db` | JPA/transaction utilities, multi-module ORM discovery |
| `auth-filter`, `server` | Jetty + Jersey wiring                              |
| `klient`, `kafka-properties` | REST/Kafka helpers                                 |
| `openapi`, `xmlutils`, `util`, `testutilities` | Misc                                               |

Known consumer exceptions (do not break these flows):
- **fp-inntektsmelding-api**: uses maskinporten for incoming tokens (not `oidc`)
- **fp-oversikt**, **fp-inntektsmelding(-api)**: custom authorization (not `abac`)

### `integrasjon/` — shared external clients

REST/GraphQL clients used by **more than one** app. Single-app integrations
stay in that app — do not promote here.

`rest-klient` is the base for building REST/GrapghQL-integrations other than clients here.

Current clients: `person-klient` (PDL), `saf-klient`,
`safselvbetjening-klient`, `dokarkiv-klient`, `ereg-klient`,
`oppgave-rest-klient`, `infotrygd-grunnlag-klient`, `spokelse-klient`,
`tilgang-klient`, `kafka-properties`.

## Release flow

SemVer library; release tags drive Dependabot in consumer repos. Breaking
changes in public APIs require a major bump and a heads-up in team channels.

## Tech

Java 25, Jakarta EE 11, Maven. Versions pinned via `fp-bom`.
