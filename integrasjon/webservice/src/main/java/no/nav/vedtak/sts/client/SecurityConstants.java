package no.nav.vedtak.sts.client;

import no.nav.vedtak.sikkerhet.domene.ConsumerId;

public class SecurityConstants {

    public static final String STS_URL_KEY = "securityTokenService.url";
    public static final String SYSTEMUSER_USERNAME = ConsumerId.SYSTEMUSER_USERNAME;
    public static final String SYSTEMUSER_PASSWORD = "systembruker.password";

    private SecurityConstants() {
        throw new IllegalAccessError("Skal ikke instansieres");
    }

}
