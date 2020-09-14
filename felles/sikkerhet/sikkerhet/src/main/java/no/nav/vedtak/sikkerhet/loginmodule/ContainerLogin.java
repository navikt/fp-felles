package no.nav.vedtak.sikkerhet.loginmodule;

import java.util.Collections;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.isso.SystemUserIdTokenProvider;
import no.nav.vedtak.isso.ressurs.TokenCallback;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;

/** Programmatisk innlogging på en tråd i containeren. Brukes av bakgrunnsjobber (eks. prosesstask) slik at disse er autentisert. */
public class ContainerLogin {
    private static final Logger log = LoggerFactory.getLogger(ContainerLogin.class);

    private final LoginContext loginContext;

    private OidcTokenHolder tokenHolder;

    public ContainerLogin() {
        loginContext = createLoginContext();
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

    private LoginContext createLoginContext() {
        CallbackHandler callbackHandler = new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (var callback : callbacks) {
                    if (callback instanceof TokenCallback) {
                        ((TokenCallback) callback).setToken(tokenHolder);
                    } else {
                        // Should never happen
                        throw new UnsupportedCallbackException(callback, TokenCallback.class + " is the only supported Callback");
                    }
                }
            }
        };
        var loginContextConfiguration = new ContainerLoginConfiguration();
        String loginAppConfiguration = ContainerLoginConfiguration.LOGIN_APP_CONFIGURATION;
        try {
            return new LoginContext(loginAppConfiguration, new Subject(), callbackHandler, loginContextConfiguration);
        } catch (LoginException le) {
            throw LoginModuleFeil.FACTORY.kunneIkkeFinneLoginmodulen(loginAppConfiguration, le).toException();
        }
    }

    private void ensureWeHaveTokens() {
        if (tokenHolder == null) {
            tokenHolder = new OidcTokenHolder(SystemUserIdTokenProvider.getSystemUserIdToken().getToken(), false);
        }
    }

    private static class ContainerLoginConfiguration extends Configuration {

        private static final String LOGIN_APP_CONFIGURATION = "TASK_OIDC";
        private static final AppConfigurationEntry[] APP_CONFIGURATION = new AppConfigurationEntry[] {
                new AppConfigurationEntry(
                    "no.nav.vedtak.sikkerhet.loginmodule.oidc.OIDCLoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                    Collections.emptyMap()),
                new AppConfigurationEntry(
                    "no.nav.vedtak.sikkerhet.loginmodule.ThreadLocalLoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    Collections.emptyMap())
        };

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            if (!LOGIN_APP_CONFIGURATION.equals(name)) {
                throw new IllegalArgumentException("Støtter kun app configuration name: " + LOGIN_APP_CONFIGURATION);
            }
            return APP_CONFIGURATION;
        }
    }

}
