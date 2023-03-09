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
        var kontekst = KONTEKST.get();
        return kontekst != null && kontekst.harKontekst();
    }

    public static Kontekst getKontekst() {
        return KONTEKST.get();
    }

    public static void setKontekst(Kontekst kontekst) {
        if (kontekst == null || !kontekst.harKontekst()) {
            throw new IllegalArgumentException("Bruk fjernKontekst");
        }
        if (harKontekst()) {
            var eksisterende = KONTEKST.get();
            LOG.info("FPFELLES KONTEKST allerede satt type {} for {} ny {} for {}", eksisterende.getContext(),
                eksisterende.getUid(), kontekst.getContext(), kontekst.getUid(), new Exception("Stracktrace/setKontekst"));
        }
        KONTEKST.set(kontekst);
    }

    public static void fjernKontekst() {
        if (harKontekst()) {
            KONTEKST.remove();
        } else {
            LOG.info("FPFELLES KONTEKST allerede fjernet", new Exception("Stracktracegenerator/fjernKontekst"));
            ;
        }
    }

}
