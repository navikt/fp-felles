package no.nav.vedtak.sikkerhet;

public class Constants {

    public static final String ID_TOKEN_COOKIE_NAME = "ID_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private Constants() {
        throw new IllegalAccessError("Skal ikke instansieres");
    }

}
