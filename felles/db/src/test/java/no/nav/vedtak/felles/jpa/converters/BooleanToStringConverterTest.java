package no.nav.vedtak.felles.jpa.converters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;

public class BooleanToStringConverterTest {

    @Test
    public void skal_konvertere_J_til_TRUE() {
        assertThat(new BooleanToStringConverter().convertToEntityAttribute("J")).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void skal_konvertere_N_til_FALSE() {
        assertThat(new BooleanToStringConverter().convertToEntityAttribute("N")).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void skal_konverte_null_string_til_null_boolean() {
        assertThat(new BooleanToStringConverter().convertToEntityAttribute(null)).isNull();
    }

    @Test
    public void skal_konverte_TRUE_til_J() {
        assertThat(new BooleanToStringConverter().convertToDatabaseColumn(Boolean.TRUE)).isEqualTo("J");
    }

    @Test
    public void skal_konverte_FALSE_til_N() {
        assertThat(new BooleanToStringConverter().convertToDatabaseColumn(Boolean.FALSE)).isEqualTo("N");
    }

    @Test
    public void skal_konverte_null_boolean_til_null_streng() {
        assertThat(new BooleanToStringConverter().convertToDatabaseColumn(null)).isEqualTo(null);
    }

}