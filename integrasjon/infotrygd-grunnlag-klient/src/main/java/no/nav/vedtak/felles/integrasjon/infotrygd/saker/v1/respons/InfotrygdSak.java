package no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons;

import java.time.LocalDate;

public record InfotrygdSak(LocalDate iverksatt, SakResultat resultat, LocalDate registrert, Saksnummer sakId, LocalDate mottatt,
                           SakType type, LocalDate vedtatt, SakValg valg, SakUndervalg undervalg, SakNivå nivaa) {



    public String saksBlokkNummer() {
        return sakId().blokk() + nrFra(sakId().nr());
    }

    public String saksNummerBlokk() {
        return nrFra(sakId().nr()) + sakId().blokk();
    }

    private static String nrFra(int nr) {
        return nr < 10 ? "0" + nr : String.valueOf(nr);
    }


    public record Saksnummer(String blokk, int nr) { }

    public interface InfotrygdKode {
        String kode();
        String termnavn();
    }

    public record SakValg(String kode, String termnavn) implements InfotrygdKode { }

    public record SakUndervalg(String kode, String termnavn) implements InfotrygdKode { }

    public record SakNivå(String kode, String termnavn) implements InfotrygdKode { }

    public record SakType(String kode, String termnavn) implements InfotrygdKode { }

    public record SakResultat(String kode, String termnavn) implements InfotrygdKode { }

}
