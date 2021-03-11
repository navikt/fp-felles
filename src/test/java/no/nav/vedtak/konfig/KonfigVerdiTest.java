package no.nav.vedtak.konfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import no.nav.vedtak.util.env.Environment;

class KonfigVerdiTest {

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

    private final int propFraFil = ENV.getProperty("test.property", int.class);
    private final int propFraFilOverride = ENV.getProperty("test1.property", int.class);
    private final int systemVinner = ENV.getProperty("test2.property", int.class);
    private final int namespaceVerdi = ENV.getProperty("test3.property", int.class);
    private final String javaHome = ENV.getProperty("user.home");
    private final String myProperty = ENV.getProperty(KonfigVerdiTest.KEY);
    private final Integer myIntegerPropertyValue = ENV.getProperty(KonfigVerdiTest.KEY_INT, Integer.class);
    private final Boolean myBooleanPropertyValue = ENV.getProperty(KonfigVerdiTest.KEY_BOOLEAN, Boolean.class);
    private final LocalDate myLocalDateValue = ENV.getProperty(KonfigVerdiTest.KEY_LOCAL_DATE, LocalDate.class);
    private final String stringProperty = ENV.getProperty("my.property", "42");
    private final Integer intDefaultProperty = ENV.getProperty("my.property", Integer.class, 42);
    private final boolean booleanDefaultProperty = ENV.getProperty("my.property", boolean.class, true);
    private final URI uriDefaultProperty = ENV.getProperty("my.property", URI.class, URI.create(NAV));
    private final int defaultNotUsed = ENV.getProperty("my.property.notdefault", int.class, 42);

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
    @EnabledIfEnvironmentVariable(named = "mvn", matches = "true")
    void propertyFil() {
        assertThat(propFraFil).isEqualTo(42);
        assertThat(propFraFilOverride).isEqualTo(200);
        assertThat(systemVinner).isEqualTo(50);
        assertThat(namespaceVerdi).isEqualTo(10);

    }

    @Test
    void defaultVerdier() {
        assertThat(defaultNotUsed).isZero();
        assertThat(stringProperty).isEqualTo("42");
        assertThat(booleanDefaultProperty).isTrue();
        assertThat(intDefaultProperty).isEqualTo(42);
        assertThat(uriDefaultProperty).isEqualTo(URI.create(NAV));
    }

    @Test
    void skal_injisere_konfig() {
        assertThat(javaHome).isNotNull();
    }

    @Test
    void skal_injisere_verdi_fra_systemproperties() {
        assertThat(myProperty).isEqualTo(VALUE);
    }

    @Test
    void skal_injisere_integer_fra_systemproperties() {
        assertThat(myIntegerPropertyValue).isEqualTo(39);
    }

    @Test
    void skal_injisere_boolean_fra_systemproperties() {
        assertThat(myBooleanPropertyValue).isFalse();
    }

    @Test
    void skal_injisere_local_date_fra_systemproperties() {
        assertThat(myLocalDateValue).isEqualTo(LocalDate.of(1989, 9, 29));
    }
}
