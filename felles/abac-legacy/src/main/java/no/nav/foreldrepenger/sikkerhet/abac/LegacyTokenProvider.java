package no.nav.foreldrepenger.sikkerhet.abac;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

@Dependent
public class LegacyTokenProvider implements TokenProvider {

    @Override
    public String getUid() {
        return SubjectHandler.getSubjectHandler().getUid();
    }

    @Override
    public SluttBruker getSluttBruker() {
        return SubjectHandler.getSubjectHandler().getSluttBruker();
    }

    @Override
    public OpenIDToken openIdToken() {
        return SubjectHandler.getSubjectHandler().getOpenIDToken();
    }

    @Override
    public String samlToken() {
        return SubjectHandler.getSubjectHandler().getSamlToken().getTokenAsString();
    }
}
