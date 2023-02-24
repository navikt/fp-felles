![](https://github.com/navikt/fp-felles/workflows/Bygg%20og%20deploy/badge.svg) 
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-felles) 
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=navikt_fp-felles)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=navikt_fp-felles)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=navikt_fp-felles)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/fp-felles)
![GitHub](https://img.shields.io/github/license/navikt/fp-felles)

# ABAC - bibliotek for fpsak som håndterer authentisering og auditloggin.
Inneholder følgende moduler:
* Audit logging med CEF (Common Event Format) som brukes i Arcsight.
* PEP/PDP biblioteker for ABAC tilgangskontroll.

### Migrering felles 3.2.x

Alle appene som benytter abac trenger å legge inn følgende dependency:
```
<dependency>
    <groupId>no.nav.foreldrepenger.felles</groupId>
    <artifactId>felles-abac</artifactId>
</dependency>
```

#### Legacy bruk
Det vil mangle en implementasjon av TokenProvider og det er mulig å implementere den selv i appen eller bruke følgende modul:
```
<dependency>
    <groupId>no.nav.foreldrepenger.felles</groupId>
    <artifactId>felles-abac-kontekst</artifactId>
</dependency>
```
Denne kommer med en avhengighet til felles-kontekst og KontekstHolder.
