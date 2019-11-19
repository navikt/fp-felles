package no.nav.vedtak.felles.integrasjon.oppgave.v1.request;

public enum OppgavePrioritet {
    //Kodene er ikke dokumentert i tjenestedokumentasjon, men er koden som blir brukt i kodeverk i GSAK
    HOY,
    NORM,
    LAV;

    public static OppgavePrioritet fraString(String kode) {
        for (OppgavePrioritet key : values()) {
            if (key.toString().equals(kode)) {
                return key;
            }
        }
        throw new IllegalArgumentException("Finner ikke prioritetkode med key: " + kode);
    }

}
