package no.nav.vedtak.sikkerhet.kontekst;

public enum IdentType {
    // Case definert av NAV "standard". Brukes i ABAC policies. Til bruk i tokenprovider/obo-logikk
    Systemressurs, // Innkommende kall fra andre systembrukere
    EksternBruker, // Bruker 11/13 siffer
    InternBruker,  // Ansatt - matcher ident og etterhvert epost
    Samhandler,    // Annen organisasjon
    Sikkerhet,     // Ingen kjent bruk - potensielt ifm pip-requests ol.
    Prosess        // Ingen kjent bruk - foreslås brukt for prosesstasks
    ;

    public boolean erSystem() {
        return Systemressurs.equals(this) || Prosess.equals(this);
    }

}
