package no.nav.vedtak.felles.integrasjon.graphql;

import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.exception.IntegrasjonException;

public class GraphQLException extends IntegrasjonException {

    public GraphQLException(String kode, List<GraphQLError> errors, URI uri) {
        super(kode, errors.stream()
                .map(GraphQLError::getMessage)
                .collect(joining(",")), null, uri);
    }
}