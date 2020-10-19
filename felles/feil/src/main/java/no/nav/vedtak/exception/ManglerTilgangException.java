package no.nav.vedtak.exception;

import java.net.URI;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;

public class ManglerTilgangException extends VLException {

    private static final String DEFAULT_MSG = "Mangler tilgang. Fikk http-kode 403 fra server %s";

    public ManglerTilgangException(Feil feil) {
        super(feil);
    }

    public ManglerTilgangException(URI endpoint, LogLevel level, Throwable cause) {
        this(new Feil("F-468815", format(DEFAULT_MSG, endpoint), level, ManglerTilgangException.class, cause));
    }

    public ManglerTilgangException(URI endpoint) {
        this(endpoint, LogLevel.ERROR, null);
    }
}
