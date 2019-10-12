package no.nav.vedtak.sikkerhet.jaspic;

import javax.servlet.annotation.WebServlet;
import java.util.Arrays;
import java.util.List;

//FIXME (u139158): PK-44123 Denne bør få et fornuftig navn, men jeg er for sliten...
public interface WSS4JProtectedServlet {
    /**
     * Validates that the pathInfo has an active endpoint that is protected by a WSS4JInInterceptor with the given requiredAction
     *
     * @param pathInfo       the pathInfo that should be checked
     * @param requiredAction the required action from WSHandlerConstants
     * @see HttpServletRequest#getPathInfo()
     * @see WSHandlerConstants
     * @see WSS4JInInterceptor
     */
    boolean isProtectedWithAction(String pathInfo, String requiredAction);

    default List<String> getUrlPatterns() {
        Class<?> clazz = this.getClass();
        if (clazz.isAnnotationPresent(WebServlet.class)) {
            return Arrays.asList(clazz.getAnnotation(WebServlet.class).urlPatterns());
        }
        throw new IllegalStateException(clazz.getName() +" mangler WebServlet annoteringen");
    }
}
