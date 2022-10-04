package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

public record Sak(String fagsakId, String fagsaksystem, Sakstype sakstype) {

    public enum Sakstype {
        FAGSAK,
        GENERELL_SAK,
        @Deprecated ARKIVSAK;

    }
}
