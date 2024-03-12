package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Status(StatusKode kode, String termnavn) implements InfotrygdKode {
}
