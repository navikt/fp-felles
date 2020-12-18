package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;

import java.io.IOException;
import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.SystemUserIdTokenProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;

public class OIDCTokenRequestFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + oidcToken());
    }

    private String oidcToken() {
        return Optional.ofNullable(suppliedToken()).orElse(exchangedToken());
    }

    private String suppliedToken() {
        return SubjectHandler.getSubjectHandler().getInternSsoToken();
    }

    private static String exchangedToken() {
        return Optional.ofNullable(samlToken())
                .map(OIDCTokenRequestFilter::exchange)
                .orElseThrow(() -> new TekniskException("F-937072", "Klarte ikke å fremskaffe et OIDC token"));
    }

    private static SAMLAssertionCredential samlToken() {
        return SubjectHandler.getSubjectHandler().getSamlToken();
    }

    private static String exchange(@SuppressWarnings("unused") SAMLAssertionCredential samlToken) {
        return SystemUserIdTokenProvider.getSystemUserIdToken().getToken();
    }
}
