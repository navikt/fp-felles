package no.nav.vedtak.sikkerhet.kontekst;

public class KontekstHolder {

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
        KONTEKST.set(kontekst);
    }

    public static void fjernKontekst() {
        if (harKontekst()) {
            KONTEKST.remove();
        }
    }

}
