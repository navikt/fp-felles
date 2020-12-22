package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.isso.SystemUserIdTokenProvider.getSystemUserIdToken;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;

class OidcTokenRequestFilter implements ClientRequestFilter, AccessTokenProvider {

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + accessToken());
    }

    @Override
    public String accessToken() {
        return Optional.ofNullable(suppliedToken())
                .orElse(exchangedToken());
    }

    private String suppliedToken() {
        return getSubjectHandler().getInternSsoToken();
    }

    private static String exchangedToken() {
        return Optional.ofNullable(samlToken())
                .map(OidcTokenRequestFilter::exchange)
                .orElseThrow(() -> new TekniskException("F-937072", "Klarte ikke å fremskaffe et OIDC token"));
    }

    private static SAMLAssertionCredential samlToken() {
        return getSubjectHandler().getSamlToken();
    }

    private static String exchange(@SuppressWarnings("unused") SAMLAssertionCredential samlToken) {
        return getSystemUserIdToken().getToken();
    }
}
