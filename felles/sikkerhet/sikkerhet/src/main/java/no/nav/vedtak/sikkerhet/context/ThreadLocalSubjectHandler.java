package no.nav.vedtak.sikkerhet.context;

import javax.security.auth.Subject;

public class ThreadLocalSubjectHandler extends SubjectHandler {

    private static ThreadLocal<Subject> subjectHolder = new ThreadLocal<>();

    @Override
    public Subject getSubject() {
        return subjectHolder.get();
    }

    public void setSubject(Subject subject) {
        subjectHolder.set(subject);
    }
}