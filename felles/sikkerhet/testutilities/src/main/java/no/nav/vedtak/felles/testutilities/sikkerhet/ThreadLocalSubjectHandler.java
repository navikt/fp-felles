package no.nav.vedtak.felles.testutilities.sikkerhet;

import javax.security.auth.Subject;

/**
 * @deprecated Bruk
 *             <li>{@code DummySubjectHandler} hvis du ikke trenger et
 *             {@code Subject}</li>
 *             <li>{@code StaticSubjectHandler} hvis trenger et {@code Subject},
 *             men ikke bryr deg om hvilket {@code Subject} du får</li>
 *             <li>{@code ThreadLocalSubjectHandler} hvis du vil ha full
 *             kontroll over hvilket {@code Subject} du får returnert</li>
 *
 * @see DummySubjectHandler
 * @see StaticSubjectHandler
 * @see no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler
 */
@Deprecated(forRemoval = true, since = "2.3.x")
public class ThreadLocalSubjectHandler extends TestSubjectHandler {

    private static ThreadLocal<Subject> subjectHolder = new ThreadLocal<>();

    @Override
    public Subject getSubject() {
        return subjectHolder.get();
    }

    @Override
    public void setSubject(Subject subject) {
        subjectHolder.set(subject);
    }

    /**
     * Sets the Subject to <code>null</code>
     */
    @Override
    public void reset() {
        setSubject(null);
    }
}