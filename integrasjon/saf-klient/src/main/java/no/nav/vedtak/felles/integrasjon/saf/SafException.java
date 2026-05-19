package no.nav.vedtak.felles.integrasjon.saf;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.List;

import no.nav.foreldrepenger.graphql.GraphQLError;
import no.nav.vedtak.exception.IntegrasjonException;

@jakarta.annotation.Generated("no.nav.foreldrepenger.graphql.codegen.GraphQLCodegen")
public class SafException extends IntegrasjonException {

    public SafException(String kode, List<GraphQLError> errors, URI uri) {
        super(kode, format("Feil %s ved GraphQL oppslag mot %s", errors.stream().map(GraphQLError::message).collect(joining(",")), uri));
    }

}
