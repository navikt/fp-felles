package no.nav.foreldrepenger.sikkerhet.abac;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@Dependent
public class LegacyTokenProvider implements TokenProvider {

    public String getUid() {
        return SubjectHandler.getSubjectHandler().getUid();
    }

    public String userToken() {
        return SubjectHandler.getSubjectHandler().getInternSsoToken();
    }

    public String samlToken() {
        return SubjectHandler.getSubjectHandler().getSamlToken().getTokenAsString();
    }
}
