package no.nav.vedtak.felles.integrasjon.person;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;
import no.nav.vedtak.exception.IntegrasjonException;

import java.net.URI;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class PdlException extends IntegrasjonException {

    private final int status;
    private final URI uri;
    private final PDLExceptionExtension extension;

    public PdlException(String kode, List<GraphQLError> errors, PDLExceptionExtension extension, int status, URI uri) {
        super(kode, format("Feil %s ved GraphQL oppslag mot %s", errors.stream()
            .map(GraphQLError::getMessage)
            .collect(joining(",")), uri));
        this.extension = extension;
        this.status = status;
        this.uri = uri;
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
