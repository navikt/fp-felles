![](https://github.com/navikt/fp-konfig/workflows/Build/badge.svg) 
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-konfig&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-konfig) 
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-konfig&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=navikt_fp-konfig)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-konfig&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=navikt_fp-konfig)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/fp-konfig)
![GitHub](https://img.shields.io/github/license/navikt/fp-konfig)

# fp-konfig 
Java basert bibliotek som brukes til å håndtere app konfigurasjon avhengig av cluster og namespace applikasjonen kjører i.
Det er mulig å injecte en spesifikk variable i en konstruktor ved å bruke `@KonfigVerdi annotering.

# Bruk

### Konfig kilder
Biblioteket leter etter konfig i følgende kilder og bruker første verdi fra kilden hvor den finnes:
- Applikasjons properties med cluster og namespace (*application-prod-fss-default.properties*)
- Applikasjons properties med cluster (*application-prod-fss.properties*)
- Applikasjons properties (*application.properties*)
- Miljø variabler (generelt alt som finnes i *System.getenv()*)
- System properties (generelt alt som finnes i *System.getProperties()*)

Det er mulig å plugge inn en egen provider ved å implementere `PropertiesKonfigVerdiProvider` klassen.

Løsningen er CDI basert.

### Bruk av `@KonfigVerdi`

Det er mulig å direkte `@Injecte` konfig som trenger ved å bruke `@KonfigVerdi` annotering i konstruktor.
KonfigVerdi minimal oppsett i konstruktor:
- ```@KonfigVerdi(value = "min.property") String minProperty```
- ```@KonfigVerdi(value = "min.property", required=false) String minProperty``` - kaster ikke exception om verdien ikke finnes.
- ```@KonfigVerdi(value = "min.property", defaultVeidi="default konfig verdi") String minProperty``` - returnerer default verdi om verdien ikke finnes i konfig.

Følgende typer støttes og kan bli returnert:
- String (default)
- Boolean/boolean
- Integer/int
- Period
- Duration
- LocalDate
- Long
- URI
- URL

### Bruk eksempler
```@KonfigVerdi(value = "test.enabled", required = false) boolean enabled```
```@KonfigVerdi(value = "bruker.navn" String bruker```
```@KonfigVerdi(value = "periode.fp") Period periode```
```@KonfigVerdi(HENDELSE_BASE_ENDPOINT) URI baseEndpoint```

### Lisens
MIT
