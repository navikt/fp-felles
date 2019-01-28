package no.nav.vedtak.sts.client;

public class SecurityConstants {

    public static final String STS_URL_KEY = "securityTokenService.url";
    public static final String SYSTEMUSER_USERNAME = "systembruker.username";
    public static final String SYSTEMUSER_PASSWORD = "systembruker.password";

    private SecurityConstants() {
        throw new IllegalAccessError("Skal ikke instansieres");
    }

}
