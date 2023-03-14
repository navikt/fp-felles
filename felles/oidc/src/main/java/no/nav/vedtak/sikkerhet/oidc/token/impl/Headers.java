package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;

/**
 * Standard header names for use in clients requesting / refreshing tokens
 */
final class Headers {

    static final String CONTENT_TYPE = "Content-type";
    static final String APPLICATION_FORM_ENCODED = "application/x-www-form-urlencoded";

    static final String AUTHORIZATION = "Authorization";
    static final String BASIC_AUTH_HEADER_PREFIX = "Basic ";

    static String basicCredentials(String username, String password) {
        return BASIC_AUTH_HEADER_PREFIX + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(UTF_8));
    }

}
