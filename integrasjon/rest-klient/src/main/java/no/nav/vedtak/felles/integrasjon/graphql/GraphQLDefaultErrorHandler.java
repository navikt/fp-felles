package no.nav.vedtak.felles.integrasjon.graphql;

import java.net.URI;
import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

public class GraphQLDefaultErrorHandler implements GraphQLErrorHandler {

    @Override
    public <T> T handleError(List<GraphQLError> errors, URI uri, String kode) {
        throw new GraphQLException(kode, errors, uri);
    }
}
