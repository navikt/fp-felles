package no.nav.vedtak.felles.integrasjon.pdl;

import static no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper.MAPPER;
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
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;

public class PdlDefaultErrorHandler implements GraphQLErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PdlDefaultErrorHandler.class);
    static final String UAUTENTISERT = "unauthenticated";
    static final String FORBUDT = "unauthorized";
    static final String UGYLDIG = "bad_request";
    static final String IKKEFUNNET = "not_found";

    @Override
    public <T> T handleError(List<GraphQLError> errors, URI uri, String kode) {
        throw errors
                .stream()
                .findFirst() // TODO hva med flere?
                .map(GraphQLError::getExtensions)
                .filter(Objects::nonNull)
                .map(PdlDefaultErrorHandler::details)
                .map(k -> exception(errors, k, uri))
                .orElseThrow(() -> exceptionFra(errors, SC_INTERNAL_SERVER_ERROR, PDL_INTERNAL, null, uri));
    }

    private static PDLExceptionExtension details(Map<String, Object> details) {
        try {
            return MAPPER.convertValue(details, PDLExceptionExtension.class);
        } catch (IllegalArgumentException e) {
            LOG.warn("Kunne ikke konvertere {} til extension", details, e);
            return null;
        }

    }

    private static PdlException exception(List<GraphQLError> errors, PDLExceptionExtension extension, URI uri) {
        if (extension == null) {
            return exceptionFra(errors, SC_INTERNAL_SERVER_ERROR, PDL_INTERNAL, extension, uri);
        }
        return switch (extension.code()) {
            case FORBUDT -> exceptionFra(errors, SC_UNAUTHORIZED, extension, uri);
            case UAUTENTISERT -> exceptionFra(errors, SC_FORBIDDEN, extension, uri);
            case IKKEFUNNET -> exceptionFra(errors, SC_NOT_FOUND, PDL_KLIENT_NOT_FOUND_KODE, extension, uri);
            case UGYLDIG -> exceptionFra(errors, SC_BAD_REQUEST, extension, uri);
            default -> exceptionFra(errors, SC_INTERNAL_SERVER_ERROR, PDL_INTERNAL, extension, uri);
        };
    }

    private static PdlException exceptionFra(List<GraphQLError> errors, int status, PDLExceptionExtension extension, URI uri) {
        return exceptionFra(errors, status, PDL_ERROR_RESPONSE, extension, uri);
    }

    private static PdlException exceptionFra(List<GraphQLError> errors, int status, String kode, PDLExceptionExtension extension, URI uri) {
        return new PdlException(kode, errors, extension, status, uri);

    }

}
