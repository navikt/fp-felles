package no.nav.vedtak.felles.integrasjon.safselvbetjening;

import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.exception.TekniskException;

public class SafErrorHandler {

    public <T> T handleError(List<GraphQLError> errors, URI uri, String kode) {
        throw new TekniskException(kode, String.format("Feil %s mot %s", errors.stream().map(GraphQLError::getMessage).collect(joining(",")), uri));
    }
}
