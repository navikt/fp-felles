# fp-felles

Inneholder følgende hovedmoduler
## Felles
* JPA / Database utilities: For å kunne dynamisk oppdage og sette sammen orm-mapping fra flere moduler.  For å sette opp lokale transaksjoner uten eksternt JTA bibliotek
* Logging / Logback utilities : For å definere log meldinger på en strukturert måte, med feilmeldingskoder (avhenger av SLF4J + Logback)
* Sikkerhet : OIDC + SAML Login moduler.  PEP/PDP biblioteker for ABAC tilgangskontroll.

## Integrasjoner

Inneholder klient konfigurasjon for å konfigurere REST/Graphql klienter og tjenester mot andre systemer i Nav.  
Skal kun inneholde klienter relevant for flere applikasjoner - hvis nødvendig å standardisere. (hvis kun en applikasjon bruker, legg det heller dit)
