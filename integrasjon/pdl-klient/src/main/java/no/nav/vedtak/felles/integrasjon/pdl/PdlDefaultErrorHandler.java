package no.nav.vedtak.felles.integrasjon.pdl;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

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
    private static final String MSG = "Feil fra PDL";

    @Override
    public <T> T handleError(List<GraphQLError> errors) {
        LOG.warn("PDL oppslag returnerte {} feil", errors.size());
        throw errors.stream()
                .findFirst() // TODO hva med flere?
                .map(GraphQLError::getExtensions)
                .map(m -> m.get("code"))
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .map(PdlDefaultErrorHandler::exception)
                .orElse(exception(SC_INTERNAL_SERVER_ERROR));
    }

    private static IntegrasjonException exception(String kode) {
        switch (kode) {
            case FORBUDT:
                return exception(SC_UNAUTHORIZED);
            case UAUTENTISERT:
                return exception(SC_FORBIDDEN);
            case IKKEFUNNET:
                return exception(SC_NOT_FOUND);
            case UGYLDIG:
                return exception(SC_BAD_REQUEST);
            default:
                return exception(SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static IntegrasjonException exception(int status) {
        return new PDLException(status, MSG);
    }
}
