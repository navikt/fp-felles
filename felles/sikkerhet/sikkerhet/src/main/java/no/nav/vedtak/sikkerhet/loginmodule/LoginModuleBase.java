package no.nav.vedtak.sikkerhet.loginmodule;

import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * baseclass for implementing the login module spec
 * @see <a href="http://download.java.net/jdk8/docs/technotes/guides/security/jaas/JAASLMDevGuide.html">http://download.java.net/jdk8/docs/technotes/guides/security/jaas/JAASLMDevGuide.html</a>
 */
public abstract class LoginModuleBase implements LoginModule {

    private final Logger logger;

    private boolean loginSuccess = false;
    private boolean commitSuccess = false;

    public LoginModuleBase(Logger logger) {
        this.logger = logger;
    }

    protected void setLoginSuccess(Boolean success){
        loginSuccess = success;
    }

    protected Boolean getLoginSuccess(){
        return loginSuccess;
    }

    @Override
    public final boolean commit() throws LoginException {
        logger.trace("enter commit");
        if (!getLoginSuccess()) {
            try {
                cleanUpLocalState();
            } catch (LoginException e) {
                logger.info("Unable to cleanUpLocalState cleanly ", e);
            }
            logger.trace("leave commit: false");
            return false;
        }
        doCommit();
        commitSuccess = true;
        return commitSuccess;
    }

    @Override
    public final boolean abort() throws LoginException {
        logger.trace("enter abort");
        if (!loginSuccess) {
            try {
                cleanUpLocalState();
            } catch (LoginException e) {
                logger.info("Unable to abort cleanly", e);
            }
            logger.trace("leave abort: false");
            return false;
        } else if (!commitSuccess) {
            cleanUpSubject();
            cleanUpLocalState();
            loginSuccess = false;
        } else {
            // Login and commit was successful, but someone else failed.
            logout();
        }
        logger.trace("leave abort: true");
        return true;
    }

    @Override
    public final boolean logout() throws LoginException {
        logger.trace("enter logout");
        cleanUpSubject();
        commitSuccess = false;
        cleanUpLocalState();
        loginSuccess = false;
        logger.trace("leave logout: true");
        return true;
    }

    /**
     * Should remove any variables instantiated in login or doCommit, but not modify the subject
     */
    protected abstract void cleanUpLocalState() throws LoginException;

    /**
     * Should remove and principals and credentials added to the subject,
     * but not destroy them nor modify and other variables instantiated in login or commit
     */
    protected abstract void cleanUpSubject();

    /**
     * Should create principals and credentials and populate the subject.
     * commit() handles the logic of when to call doCommit
     */
    protected abstract void doCommit() throws LoginException;
}
