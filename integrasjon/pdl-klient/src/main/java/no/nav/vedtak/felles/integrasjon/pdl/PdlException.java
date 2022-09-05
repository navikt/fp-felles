package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;
import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.felles.integrasjon.graphql.GraphQLException;

public class PdlException extends GraphQLException {

    private final int status;
    private final URI uri;
    private final PDLExceptionExtension extension;

    public PdlException(String kode, List<GraphQLError> errors, PDLExceptionExtension extension, int status, URI uri) {
        super(kode, errors, uri);
        this.extension = extension;
        this.status = status;
        this.uri = uri;
    }

    @Deprecated(since = "3.2", forRemoval = true)
    public PDLExceptionExtension getExtension() {
        return extension;
    }

    public PDLExceptionDetails getDetails() {
        return extension.details();
    }

    public String getCode() {
        return extension.code();
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [status=" + status + ", details=" + getDetails() + "uri=" + uri + "]";
    }
}
