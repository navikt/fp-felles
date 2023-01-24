package no.nav.vedtak.sikkerhet.oidc.token.impl;

import java.util.Optional;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;


/**
 * Tar token fra innlogget bruker og benytter dette videre
 */
public class BrukerTokenProvider {

    public static synchronized OpenIDToken getToken() {
        return Optional.ofNullable(SubjectHandler.getSubjectHandler().getOpenIDToken())
            .orElseThrow(() -> new IllegalStateException("Klarte ikke skaffe OIDC-token for :: " + SikkerhetContext.REQUEST));
    }

    public static boolean harSattBrukerOidcToken() {
        return SubjectHandler.getSubjectHandler().getInternSsoToken() != null;
    }

    public static boolean harSattBrukerSamlToken() {
        return SubjectHandler.getSubjectHandler().getSamlToken() != null;
    }

    public static String getUserId() {
        return SubjectHandler.getSubjectHandler().getUid();
    }

    public static IdentType getIdentType() {
        return SubjectHandler.getSubjectHandler().getIdentType();
    }
}
