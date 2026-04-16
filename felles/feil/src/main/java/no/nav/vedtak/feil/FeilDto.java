package no.nav.vedtak.feil;

import jakarta.validation.constraints.NotNull;

/*
 * TODO: Expand / contract på felt her. Målbilde: feiltype, feilmelding, feilreferanse. Status kan diskuteres
 *
 * FunksjonellException bør gi både feilmelding og løsningsforslag i teksten.
 * IntegrasjonsException vil ha satt statuscode + kilde-feil i getFeil.
 * Diskutere om pass-on av status og/eller kildemelding
 */
public record FeilDto(@NotNull String feilkode, @NotNull String type, @NotNull String feilKode,
                      @NotNull String feilmelding, @NotNull String message,
                      @NotNull String callId) {
    public FeilDto {
        if (feilkode == null || type == null || feilmelding == null || callId == null) {
            throw new IllegalArgumentException("Type, feilmelding, callId kan ikke være null");
        }
    }

    public FeilDto(Feilkode feilkode, String feilmelding , String callId) {
        this(feilkode.name(), feilkode.name(), feilkode.name(), feilmelding, feilmelding, callId);
    }

    public FeilDto(String feiltype, String feilmelding , String callId) {
        this(feiltype, feiltype, feiltype, feilmelding, feilmelding, callId);
    }

}
