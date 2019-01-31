package no.nav.vedtak.felles.integrasjon.behandleoppgave;

public enum PrioritetKode {
    //Kodene er ikke dokumentert i tjenestedokumentasjon, men er koden som blir brukt i kodeverk i GSAK
    HOY_FOR,
    NORM_FOR,
    LAV_FOR,
    HOY_SYK,
    NORM_SYK,
    LAV_SYK,
    HOY_OMS,
    NORM_OMS,
    LAV_OMS,
    HOY_FOS,
    NORM_FOS,
    LAV_FOS,
    HOY_STO,
    NORM_STO,
    LAV_STO;

    public static PrioritetKode fraString(String kode) {
        for (PrioritetKode key : values()) {
            if (key.toString().equals(kode)) {
                return key;
            }
        }
        throw new IllegalArgumentException("Finner ikke prioritetkode med key: " + kode);
    }

}
