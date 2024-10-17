package no.nav.vedtak.sikkerhet.kontekst;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KontekstHolderTest {

    @Test
    void testGetDefaultKontekst() {
        assertThat(KontekstHolder.harKontekst()).isFalse();
        assertThat(KontekstHolder.getKontekst()).isNotNull();
        assertThat(KontekstHolder.getKontekst().getContext()).isNull();
    }

    @Test
    void testSetAndGetSystemKontekst() {
        var eksisterende = BasisKontekst.forProsesstaskUtenSystembruker();
        KontekstHolder.setKontekst(eksisterende);

        assertThat(KontekstHolder.harKontekst()).isTrue();
        var roundtrip = KontekstHolder.getKontekst();
        assertThat(roundtrip).isNotNull();
        assertThat(roundtrip.getContext()).isEqualTo(SikkerhetContext.SYSTEM);
        assertThat(roundtrip.getUid()).isEqualTo("srvvtp");
        assertThat(roundtrip.getKompaktUid()).isEqualTo("srvvtp");
        assertThat(roundtrip.getIdentType()).isEqualTo(IdentType.Prosess);

        KontekstHolder.fjernKontekst();
        assertThat(KontekstHolder.harKontekst()).isFalse();
    }


}
