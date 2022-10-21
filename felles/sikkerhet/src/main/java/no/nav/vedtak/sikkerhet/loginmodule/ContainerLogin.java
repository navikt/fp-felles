package no.nav.vedtak.sikkerhet.loginmodule;

import java.util.Map;

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

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.ressurs.TokenCallback;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/**
 * Programmatisk innlogging på en tråd i containeren. Brukes av bakgrunnsjobber
 * (eks. prosesstask) slik at disse er autentisert.
 */
public class ContainerLogin {
    private static final Logger LOG = LoggerFactory.getLogger(ContainerLogin.class);

    private final LoginContext loginContext;

    private OpenIDToken token;

    public ContainerLogin() {
        this.loginContext = createLoginContext();
    }

    public void login() {
        ensureWeHaveTokens();
        try {
            loginContext.login();
            MDCOperations.putUserId(SubjectHandler.getSubjectHandler().getUid());
            MDCOperations.putConsumerId(SubjectHandler.getSubjectHandler().getConsumerId());
        } catch (LoginException le) {
            throw new TekniskException("F-499051", "Noe gikk galt ved innlogging", le);
        }
    }

    public void logout() {
        try {
            loginContext.logout();
        } catch (LoginException e) {
            LOG.warn("Noe gikk galt ved utlogging", e);
        }
        MDCOperations.removeUserId();
        MDCOperations.removeConsumerId();
    }

    private LoginContext createLoginContext() {
        CallbackHandler callbackHandler = new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (var callback : callbacks) {
                    if (callback instanceof TokenCallback tokenCallback) {
                        tokenCallback.setToken(token);
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
            throw new TekniskException("F-651753", String.format("Kunne ikke finne konfigurasjonen for %s", loginAppConfiguration), le);
        }
    }

    private void ensureWeHaveTokens() {
        if (token == null) {
            token = TokenProvider.getTokenFor(SikkerhetContext.SYSTEM);
        }
    }

    private static class ContainerLoginConfiguration extends Configuration {

        private static final String LOGIN_APP_CONFIGURATION = "TASK_OIDC";
        private static final AppConfigurationEntry[] APP_CONFIGURATION = new AppConfigurationEntry[] {
                new AppConfigurationEntry(
                        "no.nav.vedtak.sikkerhet.loginmodule.oidc.OIDCLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                        Map.of()),
                new AppConfigurationEntry(
                        "no.nav.vedtak.sikkerhet.loginmodule.ThreadLocalLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        Map.of())
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
