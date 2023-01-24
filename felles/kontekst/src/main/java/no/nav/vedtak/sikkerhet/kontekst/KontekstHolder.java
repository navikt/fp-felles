package no.nav.vedtak.sikkerhet.kontekst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KontekstHolder {

    private static final Logger LOG = LoggerFactory.getLogger(KontekstHolder.class);

    private static final ThreadLocal<AbstraktKontekst> KONTEKST = new ThreadLocal<>();

    private KontekstHolder() {
    }

    public static boolean harKontekst() {
        return KONTEKST.get() != null;
    }

    public static AbstraktKontekst getKontekst() {
        if (KONTEKST.get() == null) {
            LOG.info("FPFELLES KONTEKST getKontekst gir null", new Exception("Stracktrace/getKontekst"));
        }
        return KONTEKST.get();
    }

    public static void setKontekst(AbstraktKontekst kontekst) {
        var eksisterende = KONTEKST.get();
        if (eksisterende != null && kontekst != null) {
            LOG.info("FPFELLES KONTEKST allerede satt type {} for {} ny {} for {}", eksisterende.getContext(),
                eksisterende.getUid(), kontekst.getContext(), kontekst.getUid(), new Exception("Stracktrace/setKontekst"));
        }
        if (eksisterende == null && kontekst == null) {
            LOG.info("FPFELLES KONTEKST allerede satt til null", new Exception("Stracktracegenerator/setKontekst"));
        }
        KONTEKST.set(kontekst);
    }
}
