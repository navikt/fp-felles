package no.nav.vedtak.sikkerhet.loginmodule;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.util.Map;

/**
 * <p>
 * This <code>LoginModule</code> updates the Subject on the
 * ThreadLocalSubjectHandler.
 */
public class ThreadLocalLoginModule extends LoginModuleBase {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadLocalLoginModule.class);

    private Subject subject;

    public ThreadLocalLoginModule() {
        super(LOG);
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
    }

    @Override
    public boolean login() throws LoginException {
        setLoginSuccess(true);
        return true;
    }

    @Override
    public void doCommit() throws LoginException {
        getSubjectHandler().setSubject(subject);
    }

    @Override
    protected void cleanUpSubject() {
        getSubjectHandler().setSubject(null);
    }

    @Override
    protected void cleanUpLocalState() throws LoginException {
        // No state to clean
    }

    private ThreadLocalSubjectHandler getSubjectHandler() {
        var subjectHandler = SubjectHandler.getSubjectHandler();
        if (!(subjectHandler instanceof ThreadLocalSubjectHandler)) {
            throw new IllegalArgumentException(ThreadLocalLoginModule.class.getSimpleName() + " krever subject handler av klasse "
                    + ThreadLocalSubjectHandler.class + ", men fikk istedet: " + subjectHandler);
        }
        return (ThreadLocalSubjectHandler) subjectHandler;
    }

}
