package no.nav.vedtak.felles.integrasjon.saf;

import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;

public class SafErrorHandler implements GraphQLErrorHandler {

    @Override
    public <T> T handleError(List<GraphQLError> errors, URI uri, String kode) {
        throw new TekniskException(kode, errors.stream()
                .map(GraphQLError::getMessage)
                .collect(joining(",")), uri, null);
    }

}
