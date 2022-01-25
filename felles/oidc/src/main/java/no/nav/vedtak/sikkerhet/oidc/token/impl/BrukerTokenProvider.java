package no.nav.vedtak.sikkerhet.oidc.token.impl;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;


/**
 * Tar token fra innlogget bruker og benytter dette videre
 */
public class BrukerTokenProvider {

    public static synchronized OpenIDToken getToken() {
        String oidcToken = SubjectHandler.getSubjectHandler().getInternSsoToken();

        if (oidcToken != null) {
            return new OpenIDToken(oidcToken);
        }

        throw new IllegalStateException("Klarte ikke skaffe OIDC-token for :: " + SikkerhetContext.BRUKER);
    }

    public static String getUserId() {
        return SubjectHandler.getSubjectHandler().getUid();
    }
}
