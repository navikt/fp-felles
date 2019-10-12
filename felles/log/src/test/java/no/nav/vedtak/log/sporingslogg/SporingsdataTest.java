package no.nav.vedtak.log.sporingslogg;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SporingsdataTest {


    @Test
    public void skalInitialisereForMedGyldigeArgs() {
        Sporingsdata sporingsdata = Sporingsdata.opprett("login");
        assertThat(sporingsdata.getNøkler()).isEmpty();
    }

    @Test
    public void skalHuskeIder() {
        Sporingsdata sporingsdata = Sporingsdata.opprett("login");
        sporingsdata.leggTilId(StandardSporingsloggId.AKTOR_ID, "1001");
        assertThat(sporingsdata.getVerdi(StandardSporingsloggId.AKTOR_ID)).isEqualTo("1001");

        assertThat(sporingsdata.getNøkler()).containsOnly(StandardSporingsloggId.AKTOR_ID);

        sporingsdata.leggTilId(StandardSporingsloggId.AKTOR_ID, "2002");
        sporingsdata.leggTilId(StandardSporingsloggId.FNR, "1239487");
        sporingsdata.leggTilId(StandardSporingsloggId.GSAK_SAKSNUMMER, "3003");

        assertThat(sporingsdata.getNøkler()).containsOnly(StandardSporingsloggId.FNR, StandardSporingsloggId.AKTOR_ID, StandardSporingsloggId.GSAK_SAKSNUMMER);
        assertThat(sporingsdata.getVerdi(StandardSporingsloggId.FNR)).isEqualTo("1239487");
        assertThat(sporingsdata.getVerdi(StandardSporingsloggId.AKTOR_ID)).isEqualTo("2002");
        assertThat(sporingsdata.getVerdi(StandardSporingsloggId.GSAK_SAKSNUMMER)).isEqualTo("3003");
    }
}
