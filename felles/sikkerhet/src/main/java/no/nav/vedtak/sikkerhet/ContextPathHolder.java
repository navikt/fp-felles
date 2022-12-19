package no.nav.vedtak.sikkerhet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Denne eksisterer nå kun pga exp/contract + fp/k9tilbake og cookiepath /k9
 */
public class ContextPathHolder {

    private static final Logger LOG = LoggerFactory.getLogger(ContextPathHolder.class);

    private static volatile ContextPathHolder instance;
    private final String cookiePath;
    private final boolean harSattCookiePath;

    private ContextPathHolder(String cookiePath) {
        this.harSattCookiePath = cookiePath != null;
        this.cookiePath = validerCookiePath(cookiePath);
    }

    public static ContextPathHolder instance() {
        var inst = instance;
        if (inst == null) {
            inst = new ContextPathHolder(null);
            instance = inst;
        }
        return inst;
    }

    @Deprecated // K9tilbake trenger denne
    @SuppressWarnings("unused")
    public static ContextPathHolder instance(@SuppressWarnings("unused") String contextPath, String cookiePath) {
        var inst = instance;
        if (inst == null) {
            inst = new ContextPathHolder(cookiePath);
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

    public String getCookiePath() {
        return cookiePath;
    }

    public boolean harSattCookiePath() {
        return harSattCookiePath;
    }
}
