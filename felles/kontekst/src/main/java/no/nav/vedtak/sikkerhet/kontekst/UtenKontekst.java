package no.nav.vedtak.sikkerhet.kontekst;

public final class UtenKontekst extends AbstraktKontekst {

    public static final UtenKontekst INGEN = new UtenKontekst();

    private UtenKontekst() {
        super(null, null, null, null);
    }

}
