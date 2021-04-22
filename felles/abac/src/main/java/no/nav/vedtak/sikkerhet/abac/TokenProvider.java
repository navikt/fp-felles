package no.nav.vedtak.sikkerhet.abac;

public interface TokenProvider {

    String getUid();

    String userToken();

    String samlToken();

}
