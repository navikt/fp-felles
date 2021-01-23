package no.nav.vedtak.feil;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.VLException;

/**
 *
 * @deprecated Throw new {@link FunksjonellException} istedet
 */
@Deprecated
public class FunksjonellFeil extends Feil {
    private String løsningsforslag;

    public FunksjonellFeil(String kode, String feilmelding, String løsningsforslag, LogLevel logLevel, Class<? extends VLException> exceptionClass,
            Throwable cause) {
        super(kode, feilmelding, logLevel, exceptionClass, cause);
        this.løsningsforslag = løsningsforslag;
    }

    public String getLøsningsforslag() {
        return løsningsforslag;
    }

}
