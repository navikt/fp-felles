package no.nav.vedtak.sikkerhet.kontekst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KontekstHolder {

    private static final Logger LOG = LoggerFactory.getLogger(KontekstHolder.class);
    private static final BasisKontekst INGEN = BasisKontekst.tomKontekst();

    private static final ThreadLocal<Kontekst> KONTEKST = ThreadLocal.withInitial(() -> INGEN);

    private KontekstHolder() {
    }

    public static boolean harKontekst() {
        return !utenKontekst(KONTEKST.get());
    }

    public static Kontekst getKontekst() {
        var eksisterende = KONTEKST.get();
        if (utenKontekst(eksisterende)) {
            LOG.info("FPFELLES KONTEKST getKontekst gir null", new Exception("Stracktrace/getKontekst"));
        }
        return eksisterende;
    }

    public static void setKontekst(Kontekst kontekst) {
        if (utenKontekst(kontekst)) {
            throw new IllegalArgumentException("Bruk fjernKontekst");
        }
        var eksisterende = KONTEKST.get();
        if (!utenKontekst(eksisterende)) {
            LOG.info("FPFELLES KONTEKST allerede satt type {} for {} ny {} for {}", eksisterende.getContext(),
                eksisterende.getUid(), kontekst.getContext(), kontekst.getUid(), new Exception("Stracktrace/setKontekst"));
        }
        KONTEKST.set(kontekst);
    }

    public static void fjernKontekst() {
        if (utenKontekst(KONTEKST.get())) {
            LOG.info("FPFELLES KONTEKST allerede fjernet", new Exception("Stracktracegenerator/fjernKontekst"));
        } else {
            KONTEKST.remove();
        }
    }

    private static boolean utenKontekst(Kontekst kontekst) {
        return kontekst == null || !kontekst.harKontekst();
    }

}
