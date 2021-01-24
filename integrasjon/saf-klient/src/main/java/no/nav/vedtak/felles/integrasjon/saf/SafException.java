package no.nav.vedtak.felles.integrasjon.saf;

import java.net.URI;
import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.vedtak.felles.integrasjon.graphql.GraphQLException;

public class SafException extends GraphQLException {

    public SafException(String kode, List<GraphQLError> errors, URI uri) {
        super(kode, errors, uri);
    }

}
