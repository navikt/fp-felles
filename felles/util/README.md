# felles-util

## Deprecated notice
@Deprecated(since = "3.1.x", forRemoval = true)
Konfig utgår og blir ersatattet av

    <dependency>
        <groupId>no.nav.foreldrepenger</groupId>
        <artifactId>konfig</artifactId>
        <version>1.1</version>
    </dependency>

- AppLoggerFactory utgår siden sporingslogg erstattes av auditlog.
- StringUtils utgår - dra inn til app om du trenger.
- Tuple utgår bruk *org.javatuples:javatuples* isteden.

## Hensikten

Samle felles kode som ikke hører inn i de andre modulene, og som ikke fortjener en egen modul. 

## Brukes av

* Andre moduler
* Vedtaksløsningen 

