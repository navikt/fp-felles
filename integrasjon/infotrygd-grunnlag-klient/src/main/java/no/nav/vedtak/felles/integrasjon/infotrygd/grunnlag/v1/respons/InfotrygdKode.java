package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public interface InfotrygdKode {

    Enum<?> kode();

    default String getKode() {
        return kode() != null ? kode().name() : null;
    }

    String termnavn();

}
