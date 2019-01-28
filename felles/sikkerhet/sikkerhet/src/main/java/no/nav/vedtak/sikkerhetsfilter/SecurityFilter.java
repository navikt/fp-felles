package no.nav.vedtak.sikkerhetsfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import no.nav.vedtak.isso.config.ServerInfo;

@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    public static final String SWAGGER_HASH_KEY = "swagger.hash";
    /**
     * <p>
     * Dette er en base64-encodet sha256-hash av innholdet i inline script-tag i index.htm fra swagger-
     * </p>
     * <p>
     * Uten denne vil ikke swagger kjøres i nettleseren.
     * </p>
     * <p>
     * Hash for swagger i FPSAK er hardkodet i HASH_FOR_FPSAK_SWAGGER_INLINE_JAVASCRIPT.
     * NAIS applikasjoner må sette hasj som en system.property, for eksempel i JettyServer.konfigurerSwaggerHash()
     * </p>
     * <p>
     * Vedlikehold: Når innholdet i inline script-tag i index.html fra swagger endres, må denne også endres.
     * Browsere oppfordres i spesifikasjon for CSP 2.0 til å opplyse hva riktig hash er,
     * det gjør det enklelt å finne ny riktig verdi dersom innholdet endres.
     * I Chrome åpner du bare "Utviklerverktøy" -> "Console" og kopierer verdien du finner rapportert der
     * </p>
     */
    private static final String HASH_FOR_FPSAK_SWAGGER_INLINE_JAVASCRIPT = "sha256-2OFkVkSnWOWr0W45P5X5WhpI4DLkq4U03TPyK91dmfk=";
    private static final String HASH_FOR_SWAGGER_INLINE_JAVASCRIPT = getSwaggerHash();

    private static String getSwaggerHash() {
        if (System.getProperty(SWAGGER_HASH_KEY) != null) {
            return System.getProperty(SWAGGER_HASH_KEY);
        } else {
            return HASH_FOR_FPSAK_SWAGGER_INLINE_JAVASCRIPT;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //Ingenting å gjøre.
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletResponse instanceof HttpServletResponse) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setHeader("Content-Security-Policy", String.format("script-src %s '%s'", ServerInfo.instance().getSchemeHostPort(), HASH_FOR_SWAGGER_INLINE_JAVASCRIPT)); // NOSONAR her har vi full kontroll
            // TODO (u139158): Kan vi endre til å bruke self? Ref: https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy/script-src
            // response.setHeader("Content-Security-Policy", String.format("script-src %s '%s'", 'self', HASH_FOR_SWAGGER_INLINE_JAVASCRIPT)); // NOSONAR her har vi full kontroll
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-XSS-Protection", "1;mode=block");
            response.setHeader("Strict-Transport-Security", "max-age=31536000");
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        //Ingenting å gjøre.
    }
}
