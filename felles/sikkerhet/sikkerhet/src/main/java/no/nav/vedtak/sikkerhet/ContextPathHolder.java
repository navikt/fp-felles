package no.nav.vedtak.sikkerhet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextPathHolder {

    private static final Logger LOG = LoggerFactory.getLogger(ContextPathHolder.class);

    private static volatile ContextPathHolder instance; // NOSONAR
    private final String contextPath;
    private final String cookiePath;

    private ContextPathHolder(String contextPath) {
        this.contextPath = contextPath;
        this.cookiePath = validerCookiePath(null);
    }

    public ContextPathHolder(String contextPath, String cookiePath) {
        this.contextPath = contextPath;
        this.cookiePath = validerCookiePath(cookiePath);
    }

    public static ContextPathHolder instance() {
        var inst = instance;
        if (inst == null) {
            throw new IllegalStateException();
        }
        return inst;
    }

    public static ContextPathHolder instance(String contextPath) {
        var inst = instance;
        if (inst == null) {
            inst = new ContextPathHolder(contextPath);  // NOSONAR trenger ikke synkronisering her
            instance = inst;
        }
        return inst;
    }

    public static ContextPathHolder instance(String contextPath, String cookiePath) {
        var inst = instance;
        if (inst == null) {
            inst = new ContextPathHolder(contextPath, cookiePath);  // NOSONAR trenger ikke synkronisering her
            instance = inst;
        }
        return inst;
    }

    private String validerCookiePath(String cookiePath) {
        if (cookiePath == null) {
            return "/";
        }
        if (!cookiePath.startsWith("/")) {
            LOG.warn("CookiePath ({}) er ugyldig som cookiePath, forkaster og bruker default ('/').", cookiePath);
            return "/";
        }
        return cookiePath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getCookiePath() {
        return cookiePath;
    }
}
