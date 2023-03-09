package no.nav.vedtak.sikkerhet.context;

import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(ContextCleaner.class);

    private ContextCleaner() {
    }

    public static void enusureCleanContext() {
        try {
            if (KontekstHolder.harKontekst()) {
                LOG.trace("FPFELLES KONTEKST fjernet i ContextCleaner - burde vært fjernet før");
                KontekstHolder.fjernKontekst();
            }
            var subjectHandler = SubjectHandler.getSubjectHandler();
            var subject = subjectHandler.getSubject();
            // OBS JettySubjectHandler vil gi ting fra request så lenge den ikke er ferdig rensket i JASPI (etter Listener)
            if (subject != null) {
                ((ThreadLocalSubjectHandler) subjectHandler).setSubject(null);
                LOG.trace("FPFELLES ConClean: subject fjernet fra ThreadLocal");
            }
        } catch (Exception e) {
            LOG.trace("FPFELLES ConClean: kunne ikke fjerne subject", e);
        }
    }
}
