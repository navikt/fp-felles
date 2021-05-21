package no.nav.foreldrepenger.sikkerhet.abac;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@Dependent
public class LegacyTokenProvider implements TokenProvider {

    @Override
    public String getUid() {
        return SubjectHandler.getSubjectHandler().getUid();
    }

    @Override
    public String userToken() {
        return SubjectHandler.getSubjectHandler().getInternSsoToken();
    }

    @Override
    public String samlToken() {
        return SubjectHandler.getSubjectHandler().getSamlToken().getTokenAsString();
    }
}
