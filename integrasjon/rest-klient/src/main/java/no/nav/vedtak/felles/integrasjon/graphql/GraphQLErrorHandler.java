package no.nav.vedtak.felles.integrasjon.graphql;

import java.net.URI;
import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

public interface GraphQLErrorHandler {

    <T> T handleError(List<GraphQLError> errors, URI uri, String kode);

}
