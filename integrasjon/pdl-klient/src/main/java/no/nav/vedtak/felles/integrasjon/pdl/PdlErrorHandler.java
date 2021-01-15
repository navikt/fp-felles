package no.nav.vedtak.felles.integrasjon.pdl;

import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

public interface PdlErrorHandler {
    static final String UAUTENTISERT = "unauthenticated";
    static final String FORBUDT = "unauthorized";
    static final String UGYLDIG = "bad_request";
    static final String IKKEFUNNET = "not_found";;

    <T> T handleError(List<GraphQLError> e);

}
