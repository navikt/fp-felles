package no.nav.vedtak.log.sporingslogg;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SporingsdataTest {


    @Test
    public void skalInitialisereForMedGyldigeArgs() {
        Sporingsdata sporingsdata = Sporingsdata.opprett();
        assertThat(sporingsdata.getNøkler()).isEmpty();
    }

    @Test
    public void skalHuskeIder() {
        Sporingsdata sporingsdata = Sporingsdata.opprett();
        sporingsdata.leggTilId(SporingsloggId.AKSJONSPUNKT_ID, "1001");
        assertThat(sporingsdata.getVerdi(SporingsloggId.AKSJONSPUNKT_ID)).isEqualTo("1001");

        assertThat(sporingsdata.getNøkler()).containsOnly(SporingsloggId.AKSJONSPUNKT_ID);

        sporingsdata.leggTilId(SporingsloggId.AKTOR_ID, "2002");
        sporingsdata.leggTilId(SporingsloggId.ENHET_ID, "3003");

        assertThat(sporingsdata.getNøkler()).containsOnly(SporingsloggId.AKSJONSPUNKT_ID, SporingsloggId.AKTOR_ID, SporingsloggId.ENHET_ID);
        assertThat(sporingsdata.getVerdi(SporingsloggId.AKSJONSPUNKT_ID)).isEqualTo("1001");
        assertThat(sporingsdata.getVerdi(SporingsloggId.AKTOR_ID)).isEqualTo("2002");
        assertThat(sporingsdata.getVerdi(SporingsloggId.ENHET_ID)).isEqualTo("3003");
    }
}
