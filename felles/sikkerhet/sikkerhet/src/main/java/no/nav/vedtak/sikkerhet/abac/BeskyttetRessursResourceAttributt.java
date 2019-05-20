package no.nav.vedtak.sikkerhet.abac;

public enum BeskyttetRessursResourceAttributt {
    APPLIKASJON("no.nav.abac.attributter.foreldrepenger"),
    FAGSAK("no.nav.abac.attributter.foreldrepenger.fagsak"),
    VENTEFRIST("no.nav.abac.attributter.foreldrepenger.fagsak.ventefrist"),
    DRIFT("no.nav.abac.attributter.foreldrepenger.drift"),
    BATCH("no.nav.abac.attributter.foreldrepenger.batch"),
    SAKLISTE("no.nav.abac.attributter.foreldrepenger.sakliste"),
    SP_BEREGNING_APPLIKASJON("no.nav.abac.attributter.resource.sykepenger.beregning"), //TODO HUMLE: midlertidig attributt til vi finner ut hvordan Felles skal splittes mellom FP og SP
    OPPGAVEKO("no.nav.abac.attributter.foreldrepenger.oppgaveko"),
    OPPGAVESTYRING("no.nav.abac.attributter.foreldrepenger.oppgavestyring"),
    PIP("pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker"),
    OPPGAVESTYRING_AVDELINGENHET("no.nav.abac.attributter.foreldrepenger.oppgavestyring.avdelingsenhet"),
    RISIKOKLASSIFISERING("no.nav.abac.attributter.foreldrepenger.risikoklassifisering"),
    UTTAKSPLAN("no.nav.abac.attributter.resource.foreldrepenger.uttaksplan"),

    /**
     * Skal kun brukes av Interceptor
     */
    DUMMY(null);

    private String eksternKode;

    BeskyttetRessursResourceAttributt(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
