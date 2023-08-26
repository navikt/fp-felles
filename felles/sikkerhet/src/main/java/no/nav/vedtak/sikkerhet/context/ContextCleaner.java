package no.nav.vedtak.sikkerhet.context;

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
        } catch (Exception e) {
            LOG.trace("FPFELLES ConClean: kunne ikke fjerne kontekst", e);
        }
    }
}
