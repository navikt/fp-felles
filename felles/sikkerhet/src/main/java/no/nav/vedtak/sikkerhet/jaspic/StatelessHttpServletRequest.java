package no.nav.vedtak.sikkerhet.jaspic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * Wraps the request in a object that throws an {@link IllegalArgumentException}
 * when invoking getSession or getSession(true)
 */
public class StatelessHttpServletRequest extends HttpServletRequestWrapper {

    public StatelessHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (create) {
            throw new IllegalArgumentException("This is a stateless application so creating a Session is forbidden.");
        }
        return super.getSession(create);
    }
}
