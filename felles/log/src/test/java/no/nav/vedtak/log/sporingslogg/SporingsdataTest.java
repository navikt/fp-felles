package no.nav.vedtak.log.sporingslogg;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class SporingsdataTest {

    @Test
    public void skalInitialisereForMedGyldigeArgs() {
        assertThat(Sporingsdata.opprett("login").keySet()).isEmpty();
    }

    @Test
    public void skalHuskeIder() {
        Sporingsdata sporingsdata = Sporingsdata.opprett("login");
        sporingsdata.leggTilId(StandardSporingsloggId.AKTOR_ID, "1001");
        assertThat(sporingsdata.getVerdi(StandardSporingsloggId.AKTOR_ID)).isEqualTo("1001");

        assertThat(sporingsdata.keySet()).containsOnly(StandardSporingsloggId.AKTOR_ID.getSporingsloggKode());

        sporingsdata.leggTilId(StandardSporingsloggId.AKTOR_ID, "2002");
        sporingsdata.leggTilId(StandardSporingsloggId.FNR, "1239487");
        sporingsdata.leggTilId(StandardSporingsloggId.ABAC_RESOURCE_TYPE, "3003");

        assertThat(sporingsdata.keySet()).containsOnly(StandardSporingsloggId.FNR.getSporingsloggKode(),
                StandardSporingsloggId.AKTOR_ID.getSporingsloggKode(),
                StandardSporingsloggId.ABAC_RESOURCE_TYPE.getSporingsloggKode());
        assertThat(sporingsdata.getVerdi(StandardSporingsloggId.FNR)).isEqualTo("1239487");
        assertThat(sporingsdata.getVerdi(StandardSporingsloggId.AKTOR_ID)).isEqualTo("2002");
        assertThat(sporingsdata.getVerdi(StandardSporingsloggId.ABAC_RESOURCE_TYPE)).isEqualTo("3003");
    }
}
