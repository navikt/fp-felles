package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.exception.ManglerTilgangException;

public class PepNektetTilgangException extends ManglerTilgangException {
    public PepNektetTilgangException(String kode, String msg) {
        this(kode, msg, null);
    }

    private PepNektetTilgangException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}
