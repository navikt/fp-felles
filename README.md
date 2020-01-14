![](https://github.com/navikt/fp-felles/workflows/Bygg%20og%20deploy/badge.svg) 
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-felles) 
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=navikt_fp-felles)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=navikt_fp-felles)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-felles&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=navikt_fp-felles)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/fp-felles)
![GitHub](https://img.shields.io/github/license/navikt/fp-felles)

# Felles kode / bibliotek for foredrepenger området

## Postgres
I test og utvikling bruker vi postgres. For å enkelt sette opp dette slik at det fungerer med unit-tests:
```
docker rm -f fp-postgres
docker run --rm --name fp-postgres -e POSTGRES_USER=fp_unit -e POSTGRES_PASSWORD=fp_unit -e POSTGRES_DB=fp_unit  -d -p 5432:5432 postgres:alpine
```
