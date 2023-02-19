package no.nav.vedtak.sikkerhet.kontekst;

public interface Kontekst {

    SikkerhetContext getContext();

    String getUid();

    // Kortere brukernavn til bruk i tabeller: 20 tegn + historiske konvensjoner.
    String getKompaktUid();

    IdentType getIdentType();

    String getKonsumentId();

    default boolean harKontekst() {
        return getContext() != null;
    }

    default boolean erLokalSystemRessurs() {
        return SikkerhetContext.SYSTEM.equals(getContext());
    }

    default boolean erAutentisert() {
        return getIdentType() != null;
    }

}
