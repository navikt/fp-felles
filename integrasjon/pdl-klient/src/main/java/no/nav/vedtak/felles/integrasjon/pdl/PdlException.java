package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;

import no.nav.vedtak.felles.integrasjon.graphql.GraphQLException;

public class PdlException extends GraphQLException {

    private final int status;
    private final String extension;

    public PdlException(String kode, String extension, int status, URI uri) {
        super(kode, "Feil", uri);
        this.extension = extension;
        this.status = status;
    }

    public String getExtension() {
        return extension;
    }

    public int getStatus() {
        return status;
    }
}
