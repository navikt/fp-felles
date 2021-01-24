package no.nav.vedtak.felles.integrasjon.graphql;

import java.net.URI;

import no.nav.vedtak.exception.IntegrasjonException;

public class GraphQLException extends IntegrasjonException {

    public GraphQLException(String kode, String msg, URI uri) {
        super(kode, msg, null, uri);
    }

}
