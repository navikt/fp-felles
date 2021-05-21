package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.util.Optional;

public class SubjectHandlerTokenXTokenProvider implements TokenXTokenProvider {

    @Override
    public String getToken() {
        return Optional.ofNullable(getSubjectHandler().getInternSsoToken()).orElseThrow();
    }
}
