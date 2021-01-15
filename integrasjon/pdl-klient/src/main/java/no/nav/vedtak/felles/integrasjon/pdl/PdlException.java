package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;

import no.nav.vedtak.exception.IntegrasjonException;

public class PdlException extends IntegrasjonException {

    private final int status;

    public PdlException(String kode, String extension, int status, URI uri) {
        super(kode, "Feil fra PDL", null, uri, status, extension);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
