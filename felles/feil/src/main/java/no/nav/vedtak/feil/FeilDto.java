package no.nav.vedtak.feil;

import jakarta.validation.constraints.NotNull;

/*
 * FunksjonellException bør gi både feilmelding og løsningsforslag i teksten.
 * IntegrasjonsException vil ha satt statuscode + kilde-feil i getFeil.
 * Diskutere om pass-on av status og/eller kildemelding
 */
public record FeilDto(@NotNull Integer status,
                      @NotNull String feilkode,
                      @NotNull String feilmelding,
                      @NotNull String callId) {
    public FeilDto {
        if (feilkode == null || feilmelding == null || callId == null || status == null) {
            throw new IllegalArgumentException("Status, feilkode, feilmelding, callId kan ikke være null");
        }
    }

}
