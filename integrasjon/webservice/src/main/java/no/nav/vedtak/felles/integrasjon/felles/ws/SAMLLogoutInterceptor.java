package no.nav.vedtak.felles.integrasjon.felles.ws;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SAMLLogoutInterceptor implements PhaseInterceptor<SoapMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(SAMLLogoutInterceptor.class);
    private LoginContext loginContext;

    public SAMLLogoutInterceptor(LoginContext loginContext) {
        this.loginContext = loginContext;
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        logout();
    }

    @Override
    public void handleFault(SoapMessage message) {
        logout();
    }

    private void logout() {
        try {
            loginContext.logout();
        } catch (LoginException e) {
            LOG.warn("Feilet utlogging", e);
        }
    }

    @Override
    public Set<String> getAfter() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getBefore() {
        return new HashSet<>();
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getPhase() {
        return Phase.POST_INVOKE;
    }

    @Override
    public Collection<PhaseInterceptor<? extends Message>> getAdditionalInterceptors() {
        return null;
    }
}
