package no.nav.vedtak.konfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.vedtak.konfig.KonfigVerdi.StringDuplicator;

@RunWith(LocalCdiRunner.class)
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

    @Inject
    @Any
    private Instance<KonfigVerdiProvider> providers;

    @Inject
    @KonfigVerdi(value = "test.property")
    private int propFraFil;
    @Inject
    @KonfigVerdi(value = "test1.property")
    private int propFraFilOverride;
    @Inject
    @KonfigVerdi(value = "test2.property")
    private int systemVinner;

    @Inject
    @KonfigVerdi("user.home")
    private String javaHome;

    @Inject
    @KonfigVerdi(KonfigVerdiTest.KEY)
    private String myProperty;

    @Inject
    @KonfigVerdi(KonfigVerdiTest.KEY)
    private List<String> myPropertyList;

    @Inject
    @KonfigVerdi(KonfigVerdiTest.KEY)
    private Map<String, String> myPropertyMap;

    @Inject
    @KonfigVerdi(value = KonfigVerdiTest.KEY, converter = KonfigVerdi.BooleanConverter.class)
    private Map<String, Boolean> myPropertyBooleanMap;

    @Inject
    @KonfigVerdi(KonfigVerdiTest.KEY_INT)
    private String myIntegerProperty;

    @Inject
    @KonfigVerdi(value = KonfigVerdiTest.KEY_INT, converter = KonfigVerdi.IntegerConverter.class)
    private Integer myIntegerPropertyValue;

    @Inject
    @KonfigVerdi(KonfigVerdiTest.KEY_BOOLEAN)
    private String myBooleanProperty;

    @Inject
    @KonfigVerdi(value = KonfigVerdiTest.KEY_BOOLEAN, converter = KonfigVerdi.BooleanConverter.class)
    private Boolean myBooleanPropertyValue;

    @Inject
    @KonfigVerdi(value = KonfigVerdiTest.KEY_LOCAL_DATE, converter = KonfigVerdi.LocalDateConverter.class)
    private LocalDate myLocalDateValue;

    @Inject
    @KonfigVerdi(value = "my.property", defaultVerdi = "42")
    private String stringProperty;
    @Inject
    @KonfigVerdi(value = "my.property", defaultVerdi = "42", converter = StringDuplicator.class)
    private String stringDefaultWithConversionProperty;
    @Inject
    @KonfigVerdi(value = "my.property", defaultVerdi = "42")
    private Integer intDefaultProperty;
    @Inject
    @KonfigVerdi(value = "my.property", defaultVerdi = "true")
    private boolean booleanDefaultProperty;

    @Inject
    @KonfigVerdi(value = "my.property", defaultVerdi = NAV)
    private URI uriDefaultProperty;

    @Inject
    @KonfigVerdi(value = "my.property.notdefault", defaultVerdi = "42")
    private int defaultNotUsed;

    @BeforeClass
    public static void setupSystemPropertyForTest() {
        System.setProperty("my.property.notdefault", "0");
        System.setProperty(KEY, VALUE);
        System.setProperty(KEY_INT, VALUE_INT);
        System.setProperty(KEY_BOOLEAN, VALUE_BOOLEAN);
        System.setProperty(KEY_LOCAL_DATE, VALUE_LOCAL_DATE);
        System.setProperty("test2.property", "50");

    }

    @Test
    public void propertyFil() throws Exception {
        assertThat(propFraFil).isEqualTo(42);
        assertThat(propFraFilOverride).isEqualTo(200);
        assertThat(systemVinner).isEqualTo(50);
    }

    @Test
    public void defaultVerdier() throws Exception {
        assertThat(defaultNotUsed).isEqualTo(0);
        assertThat(stringProperty).isEqualTo("42");
        assertThat(booleanDefaultProperty).isEqualTo(true);
        assertThat(intDefaultProperty).isEqualTo(42);
        assertThat(stringDefaultWithConversionProperty).isEqualTo("4242");
        assertThat(uriDefaultProperty).isEqualTo(URI.create(NAV));

    }

    @Test
    public void skal_injisere_konfig() throws Exception {
        assertThat(providers).isNotEmpty();
        assertThat(javaHome).isNotNull();
    }

    @Test
    public void skal_injisere_verdi_fra_systemproperties() throws Exception {
        assertThat(providers).isNotEmpty();
        assertThat(myProperty).isEqualTo(VALUE);
    }

    @Test
    public void skal_injisere_liste_fra_systemproperties() throws Exception {
        assertThat(providers).isNotEmpty();
        assertThat(myPropertyList).isEqualTo(Arrays.asList("key1:true", "key2:false"));
    }

    @Test
    public void skal_injisere_map_fra_systemproperties() throws Exception {
        assertThat(providers).isNotEmpty();
        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "true");
        expected.put("key2", "false");

        assertThat(myPropertyMap).isEqualTo(expected);
    }

    @Test
    public void skal_injisere_boolean_map_fra_systemproperties() throws Exception {
        assertThat(providers).isNotEmpty();
        Map<String, Boolean> expected = new HashMap<>();
        expected.put("key1", Boolean.TRUE);
        expected.put("key2", Boolean.FALSE);

        assertThat(myPropertyBooleanMap).isEqualTo(expected);
    }

    @Test
    public void skal_injisere_integer_fra_systemproperties() throws Exception {
        assertThat(providers).isNotEmpty();
        int expected = 39;
        assertThat(myIntegerPropertyValue).isEqualTo(expected);
    }

    @Test
    public void skal_injisere_boolean_fra_systemproperties() throws Exception {
        assertThat(providers).isNotEmpty();
        boolean expected = false;
        assertThat(myBooleanPropertyValue).isEqualTo(expected);
    }

    @Test
    public void skal_injisere_local_date_fra_systemproperties() throws Exception {
        assertThat(providers).isNotEmpty();
        LocalDate randomDato = LocalDate.of(1989, 9, 29);
        assertThat(myLocalDateValue).isEqualTo(randomDato);
    }
}
