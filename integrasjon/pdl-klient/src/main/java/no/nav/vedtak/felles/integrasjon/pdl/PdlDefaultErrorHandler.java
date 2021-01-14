package no.nav.vedtak.felles.integrasjon.pdl;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient.PdlTjenesteFeil;

@Dependent
public class PdlDefaultErrorHandler implements PdlErrorHandler {
    private static final String PDL_ERROR_CODE = "code";
    private static final String PDL_NOT_FOUND = "not_found";
    private static final String PDL_UNAUTHORIZED = "unauthorized";
    private static final String PDL_NOT_AUTHENTICATED = "unauthenticated";
    private static final String PDL_BAD_REQUEST = "bad_request";
    private static final String PDL_SERVER_ERROR = "server_error";

    @Override
    public <T> T handleError(List<GraphQLError> errors) {
        if (errors
                .stream()
                .anyMatch(PdlDefaultErrorHandler::not_found)) {
            throw PdlTjenesteFeil.FEILFACTORY.personIkkeFunnet().toException();
        }
        var feilmelding = errors.stream()
                .map(GraphQLError::getMessage)
                .collect(Collectors.joining("\n Error: "));
        throw PdlTjenesteFeil.FEILFACTORY.forespørselReturnerteFeil(feilmelding).toException();

    }

    private static boolean not_found(GraphQLError error) {
        if (error == null || error.getExtensions() == null) {
            return false;
        }
        return PDL_NOT_FOUND.equals(error.getExtensions().get(PDL_ERROR_CODE));
    }

}
