package no.nav.vedtak.sikkerhet.kontekst;

public final class SystemKontekst extends AbstraktKontekst {

    private SystemKontekst(String uid, IdentType identType, String consumerId) {
        super(SikkerhetContext.SYSTEM, uid, identType, consumerId);
    }

    public static SystemKontekst forLokalSystemRessurs() {
        return new SystemKontekst(Systembruker.username(), IdentType.Systemressurs, Systembruker.username());
    }

    public static SystemKontekst forProsesstask() {
        return new SystemKontekst(Systembruker.username(), IdentType.Prosess, Systembruker.username());
    }

}
