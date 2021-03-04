package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;

public interface TokenProvider {
    default String getUid() {
        return SubjectHandler.getSubjectHandler().getUid();
    }

    default String userToken() {
        return SubjectHandler.getSubjectHandler().getInternSsoToken();
    }

    default String samlToken() {
        return SubjectHandler.getSubjectHandler().getSamlToken().getTokenAsString();
    }

}
