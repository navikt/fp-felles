package no.nav.vedtak.felles.integrasjon.pdl;

import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

public interface PdlErrorHandler {
    <T> T handleError(List<GraphQLError> e);

}
