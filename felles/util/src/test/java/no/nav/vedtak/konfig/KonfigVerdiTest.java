package no.nav.vedtak.konfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.util.env.Environment;

public class KonfigVerdiTest {

    private static final String NAV = "http://www.nav.no";
    private static final String KEY = "my.test.property";
    private static final String VALUE = "key1:true,key2:false";

    private static final String KEY_INT = "my.test.property.age";
    private static final String VALUE_INT = "39";

    private static final String KEY_BOOLEAN = "my.test.property.alenemor";
    private static final String VALUE_BOOLEAN = "false";

    private static final String KEY_LOCAL_DATE = "my.local.date.test";
    private static final String VALUE_LOCAL_DATE = "1989-09-29";
    private static final Environment ENV = Environment.current();

    private int propFraFil = ENV.getProperty("test.property", int.class);
    private int propFraFilOverride = ENV.getProperty("test1.property", int.class);
    private int systemVinner = ENV.getProperty("test2.property", int.class);
    private int namespaceVerdi = ENV.getProperty("test3.property", int.class);
    private String javaHome = ENV.getProperty("user.home");
    private String myProperty = ENV.getProperty(KonfigVerdiTest.KEY);
    private Integer myIntegerPropertyValue = ENV.getProperty(KonfigVerdiTest.KEY_INT, Integer.class);
    private Boolean myBooleanPropertyValue = ENV.getProperty(KonfigVerdiTest.KEY_BOOLEAN, Boolean.class);
    private LocalDate myLocalDateValue = ENV.getProperty(KonfigVerdiTest.KEY_LOCAL_DATE, LocalDate.class);
    private String stringProperty = ENV.getProperty("my.property", "42");
    private Integer intDefaultProperty = ENV.getProperty("my.property", Integer.class, 42);
    private boolean booleanDefaultProperty = ENV.getProperty("my.property", boolean.class, true);
    private URI uriDefaultProperty = ENV.getProperty("my.property", URI.class, URI.create(NAV));
    private int defaultNotUsed = ENV.getProperty("my.property.notdefault", int.class, 42);

    @BeforeAll
    public static void setupSystemPropertyForTest() {
        System.setProperty("my.property.notdefault", "0");
        System.setProperty(KEY, VALUE);
        System.setProperty(KEY_INT, VALUE_INT);
        System.setProperty(KEY_BOOLEAN, VALUE_BOOLEAN);
        System.setProperty(KEY_LOCAL_DATE, VALUE_LOCAL_DATE);
        System.setProperty("test2.property", "50");

    }

    @Test
    // Denne testen vil bare virke om den kjøres via surfire plugin.
    public void propertyFil() throws Exception {
        assertThat(propFraFil).isEqualTo(42);
        assertThat(propFraFilOverride).isEqualTo(200);
        assertThat(systemVinner).isEqualTo(50);
        assertThat(namespaceVerdi).isEqualTo(10);

    }

    @Test
    public void defaultVerdier() throws Exception {
        assertThat(defaultNotUsed).isEqualTo(0);
        assertThat(stringProperty).isEqualTo("42");
        assertThat(booleanDefaultProperty).isEqualTo(true);
        assertThat(intDefaultProperty).isEqualTo(42);
        assertThat(uriDefaultProperty).isEqualTo(URI.create(NAV));
    }

    @Test
    public void skal_injisere_konfig() throws Exception {
        assertThat(javaHome).isNotNull();
    }

    @Test
    public void skal_injisere_verdi_fra_systemproperties() throws Exception {
        assertThat(myProperty).isEqualTo(VALUE);
    }

    @Test
    public void skal_injisere_integer_fra_systemproperties() throws Exception {
        assertThat(myIntegerPropertyValue).isEqualTo(39);
    }

    @Test
    public void skal_injisere_boolean_fra_systemproperties() throws Exception {
        assertThat(myBooleanPropertyValue).isEqualTo(false);
    }

    @Test
    public void skal_injisere_local_date_fra_systemproperties() throws Exception {
        assertThat(myLocalDateValue).isEqualTo(LocalDate.of(1989, 9, 29));
    }
}
