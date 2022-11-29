package no.nav.vedtak.felles.integrasjon.saf;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;
import no.nav.vedtak.exception.TekniskException;

import java.net.URI;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class SafErrorHandler {

    public <T> T handleError(List<GraphQLError> errors, URI uri, String kode) {
        throw new TekniskException(kode, String.format("Feil %s mot %s", errors.stream()
                .map(GraphQLError::getMessage)
                .collect(joining(",")), uri));
    }
}
