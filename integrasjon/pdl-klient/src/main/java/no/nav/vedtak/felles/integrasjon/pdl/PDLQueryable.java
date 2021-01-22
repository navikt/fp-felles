package no.nav.vedtak.felles.integrasjon.pdl;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

public interface PDLQueryable {

    static final String PDL_ERROR_RESPONSE = "F-399735";
    static final String PDL_IO_EXCEPTION = "F-539237";
    static final String PDL_INTERNAL = "F-539238";
    public static final String PDL_KLIENT_NOT_FOUND_KODE = "F-399736";

    <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz);

}
