package no.nav.vedtak.felles.integrasjon.safselvbetjening;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.List;

import no.nav.foreldrepenger.graphql.GraphQLError;
import no.nav.vedtak.exception.IntegrasjonException;


public class SafException extends IntegrasjonException {

    public SafException(String kode, List<GraphQLError> errors, URI uri) {
        super(kode, format("Feil %s ved GraphQL oppslag mot %s", errors.stream().map(GraphQLError::message).collect(joining(",")), uri));
    }

}
