package no.nav.vedtak.felles.integrasjon.pdl;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;

public class PdlDefaultErrorHandler implements GraphQLErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PdlDefaultErrorHandler.class);
    private static final String UAUTENTISERT = "unauthenticated";
    private static final String FORBUDT = "unauthorized";
    private static final String UGYLDIG = "bad_request";
    private static final String IKKEFUNNET = "not_found";;

    @Override
    public <T> T handleError(List<GraphQLError> errors, URI uri, String kode) {
        LOG.warn("PDL oppslag mot {} returnerte {} feil ({})", uri, errors.size());
        throw errors
                .stream()
                .findFirst() // TODO hva med flere?
                .map(GraphQLError::getExtensions)
                .map(m -> m.get("code"))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .map(k -> exception(errors, k, uri))
                .orElseGet(() -> exceptionFra(errors, SC_INTERNAL_SERVER_ERROR, kode, "intern feil", uri));
    }

    private static IntegrasjonException exception(List<GraphQLError> errors, String extension, URI uri) {
        switch (extension) {
            case FORBUDT:
                return exceptionFra(errors, SC_UNAUTHORIZED, extension, uri);
            case UAUTENTISERT:
                return exceptionFra(errors, SC_FORBIDDEN, extension, uri);
            case IKKEFUNNET:
                return exceptionFra(errors, SC_NOT_FOUND, PDL_KLIENT_NOT_FOUND_KODE, extension, uri);
            case UGYLDIG:
                return exceptionFra(errors, SC_BAD_REQUEST, extension, uri);
            default:
                return exceptionFra(errors, SC_INTERNAL_SERVER_ERROR, PDL_INTERNAL, extension, uri);
        }
    }

    private static IntegrasjonException exceptionFra(List<GraphQLError> errors, int status, String extension, URI uri) {
        return exceptionFra(errors, status, PDL_ERROR_RESPONSE, extension, uri);
    }

    private static IntegrasjonException exceptionFra(List<GraphQLError> errors, int status, String kode, String extension, URI uri) {
        return new PdlException(kode, errors, extension, status, uri);

    }

}
