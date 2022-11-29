package no.nav.vedtak.sikkerhet.context;

import org.eclipse.jetty.server.*;

import javax.security.auth.Subject;
import java.util.Optional;

public class JettySubjectHandler extends ThreadLocalSubjectHandler {

    @Override
    public Subject getSubject() {
        return Optional.ofNullable(getSubjectFromRequest())
            .orElseGet(super::getSubject);
    }

    private static Subject getSubjectFromRequest() {
        return Optional.ofNullable(HttpConnection.getCurrentConnection())
            .map(HttpConnection::getHttpChannel)
            .map(HttpChannel::getRequest)
            .map(Request::getAuthentication)
            .filter(Authentication.User.class::isInstance)
            .map(Authentication.User.class::cast)
            .map(Authentication.User::getUserIdentity)
            .map(UserIdentity::getSubject)
            .orElse(null);
    }
}
