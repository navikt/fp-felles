package no.nav.vedtak.felles.jpa.converters;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class BooleanToStringConverterTest {

    private static final BooleanToStringConverter BOOLEAN_TO_STRING_CONVERTER = new BooleanToStringConverter();

    @Test
    public void skal_konvertere_J_til_TRUE() {
        assertThat(BOOLEAN_TO_STRING_CONVERTER.convertToEntityAttribute("J")).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void skal_konvertere_N_til_FALSE() {
        assertThat(BOOLEAN_TO_STRING_CONVERTER.convertToEntityAttribute("N")).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void skal_konverte_null_string_til_null_boolean() {
        assertThat(BOOLEAN_TO_STRING_CONVERTER.convertToEntityAttribute(null)).isNull();
    }

    @Test
    public void skal_konverte_TRUE_til_J() {
        assertThat(BOOLEAN_TO_STRING_CONVERTER.convertToDatabaseColumn(Boolean.TRUE)).isEqualTo("J");
    }

    @Test
    public void skal_konverte_FALSE_til_N() {
        assertThat(BOOLEAN_TO_STRING_CONVERTER.convertToDatabaseColumn(Boolean.FALSE)).isEqualTo("N");
    }

    @Test
    public void skal_konverte_null_boolean_til_null_streng() {
        assertThat(BOOLEAN_TO_STRING_CONVERTER.convertToDatabaseColumn(null)).isEqualTo(null);
    }

}
