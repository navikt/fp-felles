package no.nav.vedtak.exception;

import static java.lang.String.format;

import java.net.URI;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;

public class ManglerTilgangException extends VLException {

    private static final String KODE = "F-468815";
    private static final String DEFAULT_MSG = "Mangler tilgang. Fikk http-kode 403 fra server %s";

    /**
     *
     * @deprecated Lag med new og args.
     */
    @Deprecated
    public ManglerTilgangException(Feil feil) {
        super(feil);
    }

    public ManglerTilgangException(URI endpoint, LogLevel level, Throwable cause) {
        this(new Feil(KODE, format(DEFAULT_MSG, endpoint), level, ManglerTilgangException.class, cause));
    }

    public ManglerTilgangException(URI endpoint) {
        this(endpoint, LogLevel.ERROR, null);
    }
}
