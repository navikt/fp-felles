package no.nav.vedtak.felles.integrasjon.graphql;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

public interface GraphQLQueryable {

    <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz);

}
