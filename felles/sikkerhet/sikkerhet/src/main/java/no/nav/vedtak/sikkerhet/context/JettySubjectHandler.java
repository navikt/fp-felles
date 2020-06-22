package no.nav.vedtak.sikkerhet.context;

import javax.security.auth.Subject;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.HttpConnection;

public class JettySubjectHandler extends ThreadLocalSubjectHandler {

    @Override
    public Subject getSubject() {
        Subject subject = getSubjectFromRequest();
        if (subject == null) {
            subject = super.getSubject();
        }
        return subject;
    }

    @SuppressWarnings("resource")
    private Subject getSubjectFromRequest() {
        HttpConnection httpConnection = HttpConnection.getCurrentConnection();
        if (httpConnection == null) {
            return null;
        }

        Authentication authentication = httpConnection.getHttpChannel().getRequest().getAuthentication();
        if (authentication instanceof Authentication.User) {
            return ((Authentication.User) authentication).getUserIdentity().getSubject();
        }
        return null;
    }
}