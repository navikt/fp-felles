package no.nav.vedtak.felles.testutilities.sikkerhet;

import javax.security.auth.Subject;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;

public abstract class TestSubjectHandler extends SubjectHandler {

    public abstract void setSubject(Subject subject);

    /**
     * Resets the subject to default value which may be null or a concrete subject
     *
     * @see StaticSubjectHandler#reset()
     */
    public abstract void reset();
}