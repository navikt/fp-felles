package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Vedtak(Periode periode, int utbetalingsgrad, String arbeidsgiverOrgnr, Boolean erRefusjon, Integer dagsats) {

}
