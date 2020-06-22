package no.nav.vedtak.felles.integrasjon.medl2;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.function.Function;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.vedtak.feil.Feil;

public class MedlemskapsunntakTest {

    private static final long MEDL_ID_1 = 2663947L;
    private static final long MEDL_ID_2 = 2663948L;
    private static final long MEDL_ID_3 = 666L;

    private static final ObjectMapper OM;
    static {
        OM = new ObjectMapper();
        OM.registerModule(new JavaTimeModule());
        OM.registerModule(new Jdk8Module());
        OM.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        OM.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        OM.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        OM.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static String toJson(Object object, Function<JsonProcessingException, Feil> feilFactory) {
        try {
            return OM.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw (feilFactory.apply(e)).toException();
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OM.readerFor(clazz).readValue(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Feil i deserialisering");
        }
    }

    @Test
    public void roundtrip_rest_1()  {
        var mrest = new Medlemskapsunntak(MEDL_ID_1,
            LocalDate.of(2019, 8, 1),
            LocalDate.of(2019, 12, 31),
            "Full",
            "MEDFT",
            "ENDL",
            "UZB",
            null,
            true,
            new Medlemskapsunntak.Sporingsinformasjon(LocalDate.of(2020, 5, 26), "AVGSYS"),
            new Medlemskapsunntak.Studieinformasjon("VUT"));
        var json = toJson(mrest, null);
        var dser = fromJson(json, Medlemskapsunntak.class);
        assertThat(mrest).isEqualTo(dser);
        assertThat(mrest.getLovvalg()).isEqualTo(dser.getLovvalg());
        assertThat(mrest.isMedlem()).isEqualTo(dser.isMedlem());
    }

    @Test
    public void roundtrip_rest_2()  {
        var mrest = new Medlemskapsunntak(MEDL_ID_2,
            LocalDate.of(2019, 8, 1),
            LocalDate.of(2019, 12, 31),
            "FTL_2_9_1_a",
            "MEDFT",
            "ENDL",
            null,
            null,
            true,
            new Medlemskapsunntak.Sporingsinformasjon(LocalDate.of(2020, 5, 26), "AVGSYS"),
            null);
        var json = toJson(mrest, null);
        var dser = fromJson(json, Medlemskapsunntak.class);
        assertThat(mrest).isEqualTo(dser);
        assertThat(mrest.getDekning()).isEqualTo(dser.getDekning());
        assertThat(mrest.getKilde()).isEqualTo(dser.getKilde());
        assertThat(mrest.getBesluttet()).isEqualTo(dser.getBesluttet());
        assertThat(mrest.getStudieland()).isEqualTo(dser.getStudieland());
    }

    @Test
    public void roundtrip_rest_3()  {
        var mrest = new Medlemskapsunntak(MEDL_ID_3,
            LocalDate.of(2019, 1, 1),
            LocalDate.of(2019, 2, 28),
            "Full",
            "MEDFT",
            "UAVK",
            null,
            null,
            true,
            new Medlemskapsunntak.Sporingsinformasjon(LocalDate.of(2019, 5, 26), "LAANEKASSEN"),
            new Medlemskapsunntak.Studieinformasjon("SWE"));
        var json = toJson(mrest, null);
        var dser = fromJson(json, Medlemskapsunntak.class);
        assertThat(mrest).isEqualTo(dser);
        assertThat(mrest.getFraOgMed()).isEqualTo(dser.getFraOgMed());
        assertThat(mrest.getStudieland()).isEqualTo(dser.getStudieland());
    }
}
