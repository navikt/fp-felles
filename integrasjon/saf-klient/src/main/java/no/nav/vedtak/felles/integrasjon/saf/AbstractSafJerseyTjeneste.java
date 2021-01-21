package no.nav.vedtak.felles.integrasjon.saf;

import static java.util.stream.Collectors.joining;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.ws.rs.WebApplicationException;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;

public abstract class AbstractSafJerseyTjeneste extends AbstractJerseyOidcRestClient {
    protected static final String F_240613 = "F-240613";
    protected static final String DEFAULT_BASE = "https://localhost:8063/rest/api/saf";
    private static final String GRAPHQL = "/graphql";

    protected final URI base;

    public AbstractSafJerseyTjeneste(URI base) {
        this.base = base;
    }

    protected <T extends GraphQLResult<?>> T query(GraphQLOperationRequest req, GraphQLResponseProjection p, Class<T> clazz) {
        return query(new GraphQLRequest(req, p), clazz);
    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest req, Class<T> clazz) {
        try {
            return validate(client.target(base)
                    .path(GRAPHQL)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req.toHttpJsonBody()))
                    .invoke(clazz));
        } catch (WebApplicationException e) {
            throw new TekniskException(F_240613, base, e);
        }
    }

    private <T extends GraphQLResult<?>> T validate(T res) {
        if (res.hasErrors()) {
            var feil = res.getErrors().stream()
                    .map(GraphQLError::getMessage)
                    .collect(joining("\n Error: "));
            throw new TekniskException("F-588730", feil);
        }
        return res;
    }

}
