package no.nav.vedtak.felles.integrasjon.pdl;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.Dependent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.exception.IntegrasjonException;

@Dependent
public class PdlDefaultErrorHandler implements PdlErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PdlDefaultErrorHandler.class);

    @Override
    public <T> T handleError(List<GraphQLError> errors, URI uri) {
        LOG.warn("PDL oppslag returnerte {} feil", errors.size());
        throw errors.stream()
                .findFirst() // TODO hva med flere?
                .map(GraphQLError::getExtensions)
                .map(m -> m.get("code"))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .map(k -> exception(k, uri))
                .orElse(exception("F-539237", SC_INTERNAL_SERVER_ERROR, "intern feil", uri));
    }

    private static IntegrasjonException exception(String extension, URI uri) {
        switch (extension) {
            case FORBUDT:
                return exception(SC_UNAUTHORIZED, extension, uri);
            case UAUTENTISERT:
                return exception(SC_FORBIDDEN, extension, uri);
            case IKKEFUNNET:
                return exception(SC_NOT_FOUND, extension, uri);
            case UGYLDIG:
                return exception(SC_BAD_REQUEST, extension, uri);
            default:
                return exception("F-539237", SC_INTERNAL_SERVER_ERROR, extension, uri);
        }
    }

    private static IntegrasjonException exception(int status, String extension, URI uri) {
        return exception("F-399735", status, extension, uri);
    }

    private static IntegrasjonException exception(String kode, int status, String extension, URI uri) {
        return new PDLException(kode, extension, status, uri);

    }

}
