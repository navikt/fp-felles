package no.nav.vedtak.sikkerhet.abac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AbacDataAttributterTest {

    private AbacDataAttributter attributter;

    @BeforeEach
    void setUp() {
        attributter = AbacDataAttributter.opprett();
    }

    @Test
    void leggTilObjekt() {
        var attributtType = StandardAbacAttributtType.FNR;

        attributter.leggTil(attributtType, "Test1");

        assertThat(attributter.getVerdier(attributtType)).hasSize(1);
        assertThat(attributter.keySet()).hasSize(1).containsExactly(attributtType);
    }

    @Test
    void leggTilFlereSamme() {
        var attributtType = StandardAbacAttributtType.AKTØR_ID;

        attributter.leggTil(attributtType, "Test");
        attributter.leggTil(attributtType, "Test");
        attributter.leggTil(attributtType, "Test");

        assertThat(attributter.getVerdier(attributtType)).hasSize(1);
        assertThat(attributter.keySet()).hasSize(1).containsExactly(attributtType);
    }

    @Test
    void leggTilFlere() {
        var attributtType = StandardAbacAttributtType.SAKSNUMMER;

        attributter.leggTil(attributtType, "Test1");
        attributter.leggTil(attributtType, "Test2");
        attributter.leggTil(attributtType, "Test3");

        assertThat(attributter.getVerdier(attributtType)).hasSize(3);
        assertThat(attributter.keySet()).hasSize(1).containsExactly(attributtType);
    }

    @Test
    void leggTilFlereCollection() {
        var attributtType = StandardAbacAttributtType.SAKSNUMMER;

        attributter.leggTil(attributtType, List.of("Test1", "Test2", "Test3"));

        assertThat(attributter.getVerdier(attributtType)).hasSize(3);
        assertThat(attributter.keySet()).hasSize(1).containsExactly(attributtType);
    }

    @Test
    void leggTilEnCollection() {
        var attributtType = StandardAbacAttributtType.AKSJONSPUNKT_KODE;

        attributter.leggTil(attributtType, "Test");
        attributter.leggTil(attributtType, List.of("Test1", "Test2", "Test3"));

        assertThat(attributter.getVerdier(attributtType)).hasSize(4);
        assertThat(attributter.keySet()).hasSize(1).containsExactly(attributtType);
    }


    @Test
    void leggTilEnAnnenAttributeType() {
        var attributtType = StandardAbacAttributtType.AKSJONSPUNKT_KODE;

        attributter.leggTil(attributtType, "Test");

        var annenType = AbacDataAttributter.opprett().leggTil(attributtType, "Test1");
        attributter.leggTil(annenType);

        assertThat(attributter.getVerdier(attributtType)).hasSize(2);
        assertThat(attributter.keySet()).hasSize(1).containsExactly(attributtType);
    }

    @Test
    void toStringTest() {
        var attributtType = StandardAbacAttributtType.FNR;

        var typer = attributter.leggTil(attributtType, "Test");

        assertThat(typer.toString()).contains("MASKERT");
    }

    @Test
    void equalsTrueTest() {
        var attributtType = StandardAbacAttributtType.FNR;

        attributter.leggTil(attributtType, "Test");

        var annenType = AbacDataAttributter.opprett().leggTil(attributter);

        assertThat(attributter.equals(annenType)).isTrue();
    }

    @Test
    void equalsFalseTest() {
        var attributtType = StandardAbacAttributtType.FNR;

        attributter.leggTil(attributtType, "Test");

        var annenType = AbacDataAttributter.opprett().leggTil(attributtType, "Test2");

        assertThat(attributter.equals(annenType)).isFalse();
    }

    @Test
    void equalsWrongInstanceTest() {
        var attributtType = StandardAbacAttributtType.FNR;
        attributter.leggTil(attributtType, "Test");

        assertThat(attributter.equals(attributtType)).isFalse();
    }

    @Test
    void hashCodeTest() {
        var attributtType = StandardAbacAttributtType.FNR;
        attributter.leggTil(attributtType, "Test");

        assertThat(attributter.hashCode()).isNotNull();
    }
}
