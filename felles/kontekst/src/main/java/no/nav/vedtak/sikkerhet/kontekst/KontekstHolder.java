package no.nav.vedtak.sikkerhet.kontekst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KontekstHolder {

    private static final Logger LOG = LoggerFactory.getLogger(KontekstHolder.class);

    private static final ThreadLocal<AbstraktKontekst> KONTEKST = ThreadLocal.withInitial(() -> UtenKontekst.INGEN);

    private KontekstHolder() {
    }

    public static boolean harKontekst() {
        return KONTEKST.get() != null;
    }

    public static AbstraktKontekst getKontekst() {
        if (erUtenKontekst(KONTEKST.get())) {
            LOG.info("FPFELLES KONTEKST getKontekst gir null", new Exception("Stracktrace/getKontekst"));
        }
        return KONTEKST.get();
    }

    public static void setKontekst(AbstraktKontekst kontekst) {
        var eksisterende = KONTEKST.get();
        if (!erUtenKontekst(eksisterende) && !erUtenKontekst(kontekst)) {
            LOG.info("FPFELLES KONTEKST allerede satt type {} for {} ny {} for {}", eksisterende.getContext(),
                eksisterende.getUid(), kontekst.getContext(), kontekst.getUid(), new Exception("Stracktrace/setKontekst"));
        }
        if (erUtenKontekst(eksisterende) && erUtenKontekst(kontekst)) {
            LOG.info("FPFELLES KONTEKST allerede satt til null", new Exception("Stracktracegenerator/setKontekst"));
        }
        KONTEKST.set(kontekst);
    }

    public static void fjernKontekst() {
        if (erUtenKontekst(KONTEKST.get())) {
            LOG.info("FPFELLES KONTEKST allerede fjernet", new Exception("Stracktracegenerator/fjernKontekst"));
        }
        KONTEKST.remove();
    }

    private static boolean erUtenKontekst(AbstraktKontekst kontekst) {
        return kontekst == null || UtenKontekst.INGEN.equals(kontekst);
    }

}
