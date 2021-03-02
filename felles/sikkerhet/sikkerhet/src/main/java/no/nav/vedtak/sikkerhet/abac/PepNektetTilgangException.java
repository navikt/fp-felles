package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;

public class PepNektetTilgangException extends ManglerTilgangException {
    public PepNektetTilgangException(String kode, String msg) {
        this(kode, msg, null);
    }

    public PepNektetTilgangException(String kode, String msg, Throwable cause) {
        this(kode, msg, WARN, ManglerTilgangException.class, cause);
    }

    private PepNektetTilgangException(String kode, String msg, LogLevel level, Class<? extends VLException> clazz, Throwable cause) {
        super(new Feil(kode, msg, level, clazz, cause));
    }
}
