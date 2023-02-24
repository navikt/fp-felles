Denne modulen dekker JASPI, JAAS og Tokenvalidering
-

Kode relatert til Context, OIDC-config og Tokenhenting/veksling ligger i felles-oidc

Sikkerhetskontekst skal hentes fra KontekstHolder, ikke via SubjectHandler

Kort om hovedkomponentene

OidcAuthModule
* Inngangsportalen og konfigureres programmatisk i applikasjonenes Jetty-oppsett
* validateRequest får inn alle requests og beskyttede ressurser håndteres enten av SAML-logn eller lokal oidcLogin
* Login lager en LoginContext og forsøker en login (validere token, sette context)
* Dersom unathorized: Hvis Bearer -> 401, ellers redirect til OpenAm-login
* Spesialhåndtering av interaktive requests auth-flow, cookies og refresh 2 minutt før tokenutløp
* Stateless connection - ingen session

LoginModule(s) 
* Gjør tokenvalidering og fortsetter prosess basert på resultat
* Vil sørge for en kontekst bestående av Subject, Principal m/identType og token
* Får inn subject og callback i initialize()
* Vanlig OIDC-requests håndteres ved tokenvalidering og setter Authentication for request
* SAML-requests håndteres ved tokenvalidering og setter en trådlokal kontekst

OidcTokenValidator validerer tokens fra ulike issuers og henter jwks til keycache.
Er for tiden en blanding av jose4j og nimbusds (og litt no nav sikkerhet)

SAML-relaterte ting ligger i integrasjon/webservice
