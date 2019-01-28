package no.nav.vedtak.sikkerhet.loginmodule;

import static no.nav.vedtak.sikkerhet.loginmodule.LoginConfigNames.TASK_OIDC;

import javax.enterprise.inject.spi.CDI;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.isso.SystemUserIdTokenProvider;
import no.nav.vedtak.isso.ressurs.TokenCallback;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;

public class ContainerLogin {
    private static final Logger log = LoggerFactory.getLogger(ContainerLogin.class);

    private final LoginContext loginContext;

    private OidcTokenHolder tokenHolder;

    public ContainerLogin() {
        // No need for bean.destroy(instance) since it's ApplicationScoped
        LoginContextConfiguration loginContextConfiguration = CDI.current().select(LoginContextConfiguration.class).get();
        loginContext = createLoginContext(TASK_OIDC, loginContextConfiguration);
    }

    public void login() {
        ensureWeHaveTokens();
        try {
            loginContext.login();
            MDCOperations.putUserId(SubjectHandler.getSubjectHandler().getUid());
            MDCOperations.putConsumerId(SubjectHandler.getSubjectHandler().getConsumerId());
        } catch (LoginException le) {
            throw LoginModuleFeil.FACTORY.feiletInnlogging(le).toException();
        }
    }

    public void logout() {
        try {
            loginContext.logout();
        } catch (LoginException e) {
            LoginModuleFeil.FACTORY.feiletUtlogging(e).log(log);
        }
        MDCOperations.removeUserId();
        MDCOperations.removeConsumerId();
    }

    private LoginContext createLoginContext(LoginConfigNames loginConfigName, LoginContextConfiguration loginContextConfiguration) {
        CallbackHandler callbackHandler = new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof TokenCallback) {
                        ((TokenCallback) callback).setToken(tokenHolder);
                    } else {
                        // Should never happen
                        throw new UnsupportedCallbackException(callback, PasswordCallback.class + " is the only supported Callback");
                    }
                }
            }
        };
        try {
            return new LoginContext(loginConfigName.name(), new Subject(), callbackHandler, loginContextConfiguration);
        } catch (LoginException le) {
            throw LoginModuleFeil.FACTORY.kunneIkkeFinneLoginmodulen(loginConfigName.name(), le).toException();
        }
    }

    private void ensureWeHaveTokens() {
        if (tokenHolder == null) {
            tokenHolder = new OidcTokenHolder(SystemUserIdTokenProvider.getSystemUserIdToken().getToken(), false);
        }
    }

}
