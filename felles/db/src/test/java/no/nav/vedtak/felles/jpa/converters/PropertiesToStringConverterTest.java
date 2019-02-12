package no.nav.vedtak.felles.jpa.converters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PropertiesToStringConverterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test_method_convertToDatabaseColumn() {
        final String propNavn = "propNavn";
        final String propVerdi = "propVerdi";
        Properties properties = new Properties();
        properties.setProperty(propNavn, propVerdi);
        String converted = new PropertiesToStringConverter().convertToDatabaseColumn(properties);
        assertThat(converted).isEqualTo(new StringBuffer(propNavn).append("=").append(propVerdi).append("\n").toString());
    }

    @Test
    public void test_method_convertToDatabaseColumn_null_sjekk() {
        Properties properties = new Properties();
        assertNull(new PropertiesToStringConverter().convertToDatabaseColumn(properties));
    }

    @Test
    public void test_method_convertToEntityAttribute() {
        final String dbData = "navn=testNavn\nby=oslo\nland=norge";
        Properties properties = new PropertiesToStringConverter().convertToEntityAttribute(dbData);
        assertThat(properties.getProperty("land")).isEqualTo("norge");
    }

}
