package no.nav.vedtak.sikkerhet.oidc.token;

public record TokenString(String token) {

    @Override
    public String toString() {
        return "token";
    }

}
