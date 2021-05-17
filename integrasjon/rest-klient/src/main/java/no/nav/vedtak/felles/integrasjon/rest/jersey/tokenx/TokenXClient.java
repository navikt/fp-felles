package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

public interface TokenXClient {

    String exchange(String token, TokenXAudience audience);

}
