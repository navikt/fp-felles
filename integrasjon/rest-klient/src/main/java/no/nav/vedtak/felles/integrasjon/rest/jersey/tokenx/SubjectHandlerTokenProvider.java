package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubjectHandlerTokenProvider implements TokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectHandlerTokenProvider.class);

    @Override
    public String getToken() {
        return Optional.ofNullable(getSubjectHandler().getInternSsoToken()).orElseThrow();
    }

}
