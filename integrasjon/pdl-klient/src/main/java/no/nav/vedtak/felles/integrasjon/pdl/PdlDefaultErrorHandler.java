package no.nav.vedtak.felles.integrasjon.pdl;

import static java.util.stream.Collectors.toList;
import static no.nav.vedtak.felles.integrasjon.pdl.Pdl.PDL_ERROR_RESPONSE;
import static no.nav.vedtak.felles.integrasjon.pdl.Pdl.PDL_INTERNAL;
import static no.nav.vedtak.felles.integrasjon.pdl.Pdl.PDL_KLIENT_NOT_FOUND_KODE;
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
        LOG.warn("PDL oppslag mot {} returnerte {} feil ({})", uri, errors.size(), errorMsgs(errors));
        throw errors
                .stream()
                .findFirst() // TODO hva med flere?
                .map(GraphQLError::getExtensions)
                .map(m -> m.get("code"))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .map(k -> exception(k, uri))
                .orElseGet(() -> exceptionFra(SC_INTERNAL_SERVER_ERROR, PDL_INTERNAL, "intern feil", uri));
    }

    private static List<String> errorMsgs(List<GraphQLError> errors) {
        return errors
                .stream()
                .map(GraphQLError::getMessage)
                .collect(toList());
    }

    private static IntegrasjonException exception(String extension, URI uri) {
        switch (extension) {
            case FORBUDT:
                return exceptionFra(SC_UNAUTHORIZED, extension, uri);
            case UAUTENTISERT:
                return exceptionFra(SC_FORBIDDEN, extension, uri);
            case IKKEFUNNET:
                return exceptionFra(SC_NOT_FOUND, PDL_KLIENT_NOT_FOUND_KODE, extension, uri);
            case UGYLDIG:
                return exceptionFra(SC_BAD_REQUEST, extension, uri);
            default:
                return exceptionFra(SC_INTERNAL_SERVER_ERROR, PDL_INTERNAL, extension, uri);
        }
    }

    private static IntegrasjonException exceptionFra(int status, String extension, URI uri) {
        return exceptionFra(status, PDL_ERROR_RESPONSE, extension, uri);
    }

    private static IntegrasjonException exceptionFra(int status, String kode, String extension, URI uri) {
        return new PdlException(kode, extension, status, uri);

    }

}
