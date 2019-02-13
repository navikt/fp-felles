package no.nav.vedtak.sikkerhet;

public class ContextPathHolder {

    private static volatile ContextPathHolder instance = null;
    private final String contextPath;

    private ContextPathHolder(String contextPath) {
        this.contextPath = contextPath;
    }

    public static ContextPathHolder instance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public static ContextPathHolder instance(String contextPath) {
        if (instance == null) {
            synchronized (ContextPathHolder.class) {
                if (instance == null) {
                    instance = new ContextPathHolder(contextPath);
                }
            }
        }
        return instance;
    }

    public String getContextPath() {
        return contextPath;
    }

}
