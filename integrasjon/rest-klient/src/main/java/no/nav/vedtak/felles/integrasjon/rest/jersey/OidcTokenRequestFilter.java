package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.Priorities.AUTHENTICATION;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.util.Optional;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.sikkerhet.context.containers.SAMLAssertionCredential;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/**
 * Dette filteret erstatter {@link OidcRestClient} og er funksjonelt ekvivalent
 * med denne. Typisk bruk er i tjenester som trenger et OIDC bearer token i en
 * AUTHORIZATION header og vil normalt brukes sammen med
 * {@link AbstractJerseyOidcRestClient}, som automatisk registrer dette filteret
 * *
 */
@Priority(AUTHENTICATION)
public class OidcTokenRequestFilter implements ClientRequestFilter, AccessTokenProvider {

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + accessToken());
    }

    @Override
    public String accessToken() {
        return Optional.ofNullable(suppliedToken())
                .orElseGet(this::exchangedToken);
    }

    protected String suppliedToken() {
        try {
            return TokenProvider.getTokenFor(SikkerhetContext.BRUKER).token();
        } catch (Exception e) {
            return null;
        }
    }

    protected String exchangedToken() {
        return Optional.ofNullable(samlToken())
                .map(this::exchange)
                .orElseThrow(() -> new TekniskException("F-937072", "Klarte ikke å fremskaffe et OIDC token"));
    }

    protected SAMLAssertionCredential samlToken() {
        return getSubjectHandler().getSamlToken();
    }

    private String exchange(@SuppressWarnings("unused") SAMLAssertionCredential samlToken) {
        return TokenProvider.getTokenFor(SikkerhetContext.SYSTEM).token();
    }

}
