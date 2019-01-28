package no.nav.vedtak.felles.testutilities.sikkerhet;

import javax.security.auth.Subject;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;

public class DummySubjectHandler extends SubjectHandler {
    @Override
    public Subject getSubject() {
        return null;
    }
}
