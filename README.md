[![Bygg](https://github.com/navikt/fp-felles/actions/workflows/build.yml/badge.svg)](https://github.com/navikt/fp-felles/actions/workflows/build.yml)

[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-felles)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=navikt_fp-felles)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=navikt_fp-felles)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=navikt_fp-felles)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=navikt_fp-felles)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=navikt_fp-felles)

![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/fp-felles)
![GitHub](https://img.shields.io/github/license/navikt/fp-felles)

# fp-felles

Inneholder følgende hovedmoduler
## Felles
* JPA / Database utilities: For å kunne dynamisk oppdage og sette sammen orm-mapping fra flere moduler.  For å sette opp lokale transaksjoner uten eksternt JTA bibliotek
* Logging / Logback utilities : For å definere log meldinger på en strukturert måte, med feilmeldingskoder (avhenger av SLF4J + Logback)
* Auentisering og tokens : Tokenvalidering for innkommende kall + tokenproviders/veksling for utgående kall.  
* Tilgangskontroll: PEP/PDP biblioteker for ABAC tilgangskontroll.
* Byggesteiner: En rekke mindre moduler - Json, Xml, etc.

## Integrasjoner

Inneholder klient konfigurasjon for å konfigurere REST/Graphql klienter og tjenester mot andre systemer i Nav.  
Skal kun inneholde klienter relevant for flere applikasjoner - hvis nødvendig å standardisere. (hvis kun en applikasjon bruker, legg det heller dit)
