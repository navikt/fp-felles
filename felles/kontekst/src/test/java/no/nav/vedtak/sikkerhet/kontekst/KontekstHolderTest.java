package no.nav.vedtak.sikkerhet.kontekst;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KontekstHolderTest {

    @Test
    void testGetDefaultKontekst() {
        assertThat(KontekstHolder.harKontekst()).isFalse();
        assertThat(KontekstHolder.getKontekst()).isNull();
    }

    @Test
    void testSetAndGetSystemKontekst() {
        var eksisterende = SystemKontekst.forProsesstask();
        KontekstHolder.setKontekst(eksisterende);

        assertThat(KontekstHolder.harKontekst()).isTrue();
        var roundtrip = KontekstHolder.getKontekst();
        assertThat(roundtrip).isNotNull();
        assertThat(roundtrip.getContext()).isEqualTo(SikkerhetContext.SYSTEM);
        assertThat(roundtrip.getUid()).isEqualTo(Systembruker.username());
        assertThat(roundtrip.getIdentType()).isEqualTo(IdentType.Prosess);

        KontekstHolder.setKontekst(null);
        assertThat(KontekstHolder.harKontekst()).isFalse();
    }


}
