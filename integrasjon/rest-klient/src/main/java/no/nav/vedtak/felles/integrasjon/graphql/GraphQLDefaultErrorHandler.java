package no.nav.vedtak.felles.integrasjon.graphql;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

public class GraphQLDefaultErrorHandler implements GraphQLErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GraphQLDefaultErrorHandler.class);

    @Override
    public <T> T handleError(List<GraphQLError> errors, URI uri) {
        LOG.warn("PDL oppslag mot {} returnerte {} feil ({})", uri, errors.size(), errorMsgs(errors));
        var feilmelding = errors.stream()
                .map(error -> error.getMessage())
                .collect(Collectors.joining("\n Error: "));
        throw new GraphQLException("F-999999", feilmelding, uri);
    }

    private static List<String> errorMsgs(List<GraphQLError> errors) {
        return errors
                .stream()
                .map(GraphQLError::getMessage)
                .collect(toList());
    }
}
