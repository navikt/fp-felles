package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;
import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.felles.integrasjon.graphql.GraphQLException;

public class PdlException extends GraphQLException {

    private final int status;
    private final PDLExceptionExtension extension;

    public PdlException(String kode, List<GraphQLError> errors, PDLExceptionExtension extension, int status, URI uri) {
        super(kode, errors, uri);
        this.extension = extension;
        this.status = status;
    }

    public PDLExceptionExtension getExtension() {
        return extension;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [status=" + status + ", extension=" + extension + "]";
    }
}
