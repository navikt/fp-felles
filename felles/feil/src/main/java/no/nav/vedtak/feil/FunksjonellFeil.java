package no.nav.vedtak.feil;

import no.nav.vedtak.exception.VLException;

public class FunksjonellFeil extends Feil {
    private String løsningsforslag;

    FunksjonellFeil(String kode, String feilmelding, String løsningsforslag, LogLevel logLevel, Class<? extends VLException> exceptionClass, Throwable cause) {
        super(kode, feilmelding, logLevel, exceptionClass, cause);
        this.løsningsforslag = løsningsforslag;
    }

    public String getLøsningsforslag() {
        return løsningsforslag;
    }

}
