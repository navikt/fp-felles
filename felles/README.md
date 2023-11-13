Felles biblioteker, som dekker

# Logging
	sporingslogg for sikkerhet, noe påbygg for å logge prosesskontekst via SLF4j MDC
# Feilhåndtering 
	standard for exceptions, logfeilmeldinger, feilmelding koder
# Sikkerhet 
	OIDC Autentisering, Azure + STS - Security Token Service for veksling av tokens, ABAC (PEP/PDP)
# Database oppsett/tillegg
	* Støtter JPA ORM XML konfigurasjon i flere filer (auto-discovery). Gjør det mulig å dele opp datamodellen i ulike moduler.
	* Støtter for tilgang til flere skjemaer gjennom samme datasource (uten å hardkode schema navn i hibernate). Gjør det mulig å dele opp database i flere skjemaer
	* Støtte for å lage EntityManager og transaction annotation uten å trenge en egen JTA provider (bruker kun Databasen sin Transaction Manager)

# Testutilites
	For å kjøre enhetstester.
	* CdiRunner - JUnit Runner (bruk med @RunWith) for å kjøre test med CDI injection
	* RepositoryRule - for å kjøre en test med database tilgang (og EntityManager initialisert).  
	* StillTid - for å endre tid en test kjører i (endrer Java Clock. Krever at kode henter dato gjennom FPDateUtil.
	* Whitebox - erstatter metoder fjernet i Mockito for å overstyre felter (inntil de er skrevet vekk fra tester so bruker det)
	* 
	
	
	
