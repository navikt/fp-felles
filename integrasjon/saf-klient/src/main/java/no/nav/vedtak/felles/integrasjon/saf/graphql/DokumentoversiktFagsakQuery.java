package no.nav.vedtak.felles.integrasjon.saf.graphql;

import javax.validation.constraints.NotNull;

public class DokumentoversiktFagsakQuery implements SafQuery {

    @NotNull
    private String fagsakId;

    @NotNull
    private String fagsaksystem;

    public DokumentoversiktFagsakQuery(@NotNull String fagsakId, @NotNull String fagsaksystem) {
        this.fagsakId = fagsakId;
        this.fagsaksystem = fagsaksystem;
    }

    public String getFagsakId() {
        return fagsakId;
    }

    public String getFagsaksystem() {
        return fagsaksystem;
    }

    @Override
    public String toString() {
        return "DokumentoversiktFagsakQuery{" +
            "fagsakId='" + fagsakId + '\'' +
            ", fagsaksystem='" + fagsaksystem + '\'' +
            '}';
    }
}
