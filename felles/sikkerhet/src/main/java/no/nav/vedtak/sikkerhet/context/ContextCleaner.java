package no.nav.vedtak.sikkerhet.context;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

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
            // OBS viss fare for at JettySubjectHandler kan gi ting fra request så lenge den ikke er ferdig destroyed
            if (subject != null ) {
                final Set<String> credidentialClasses = subject.getPublicCredentials().stream()
                    .map(pc -> pc.getClass().getName())
                    .collect(Collectors.toSet());
                if (!credidentialClasses.isEmpty() || !subject.getPrincipals().isEmpty()) {
                    LOG.info("FPFELLES ConClean: inneholdt principals {} og PublicCredentials klasser {}",subject.getPrincipals(), credidentialClasses);
                }
                ((ThreadLocalSubjectHandler) subjectHandler).setSubject(null);
                LOG.trace("FPFELLES ConClean: subject fjernet fra ThreadLocal");
            }
        } catch (Exception e) {
            LOG.trace("FPFELLES ConClean: kunne ikke fjerne subject", e);
        }
    }
}
