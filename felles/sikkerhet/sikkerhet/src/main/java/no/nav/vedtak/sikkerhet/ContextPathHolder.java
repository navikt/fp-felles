package no.nav.vedtak.sikkerhet;

public class ContextPathHolder {

    private static volatile ContextPathHolder instance; // NOSONAR
    private final String contextPath;

    private ContextPathHolder(String contextPath) {
        this.contextPath = contextPath;
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

    public String getContextPath() {
        return contextPath;
    }

}
