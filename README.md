![](https://github.com/navikt/fp-konfig/workflows/Build/badge.svg) 
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-konfig&metric=alert_status)](https://sonarcloud.io/dashboard?id=navikt_fp-konfig) 
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-konfig&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=navikt_fp-konfig)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=navikt_fp-konfig&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=navikt_fp-konfig)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/navikt/fp-konfig)
![GitHub](https://img.shields.io/github/license/navikt/fp-konfig)

# fp-konfig 
Java/CDI basert bibliotek som brukes til å håndtere app konfigurasjon avhengig av kubernetes cluster og namespace applikasjonen kjører i.

Det er mulig å injecte en spesifikk variable i en konstruktor eller felt ved å bruke ``@KonfigVerdi`` annotering.

## Bruk

### Konfig kilder
Biblioteket leter etter konfig i følgende kilder og bruker første verdi fra kilden hvor den finnes.

Prioriteten på kilder er som følge:
- Applikasjons properties med cluster og namespace (*application-prod-fss-default.properties*)
- Applikasjons properties med cluster (*application-prod-fss.properties*)
- Applikasjons properties (*application.properties*)
- Miljø variabler (generelt alt som finnes i *System.getenv()*)
- System properties (generelt alt som finnes i *System.getProperties()*)

Det er mulig å plugge inn en egen provider ved å implementere `PropertiesKonfigVerdiProvider` klassen.

Løsningen er CDI basert.

### Bruk av `@KonfigVerdi`

Det er mulig å direkte `@Injecte` konfig som trenger ved å bruke `@KonfigVerdi` annotering i konstruktor eller direkte på et attribut.
Minimum oppsett:
- ```@KonfigVerdi(value = "min.property") String minProperty```
- ```@KonfigVerdi(value = "min.property", required=false) String minProperty``` - kaster ikke exception om verdien ikke finnes.
- ```@KonfigVerdi(value = "min.property", defaultVeidi="default konfig verdi") String minProperty``` - returnerer default verdi om verdien ikke finnes i konfig.
- ```
    @KonfigVerdi(value = "min.property")
    private String minProperty

Følgende typer støttes og kan bli returnert:
- String (default)
- Boolean / boolean
- Integer / int
- Period
- Duration
- LocalDate
- Long
- URI
- URL

### Bruk eksempler
- ```@KonfigVerdi(value = "test.enabled", required = false) boolean enabled```
- ```@KonfigVerdi(value = "bruker.navn" String bruker```
- ```@KonfigVerdi(value = "periode.fp") Period periode```
- ```@KonfigVerdi(value = HENDELSE_BASE_ENDPOINT, defaultValue=DEFAULT_BASE_ENDPOINT) URI baseEndpoint```

### Utilities
- `Environment` - statisk klasse som gir informasjon om miljøet appen kjører i.
- `Cluster` - statisk klasse med info om clusteret appen kjører i f.eks: isProd(), isDev(), isLocal(), etc.
- `Namespace` - statisk klasse som leverer egenskaper om namespacet appen kjører i f.eks: getName()

### Lisens
MIT
