package no.nav.vedtak.felles.integrasjon.infotrygd.saker;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.InfotrygdSak;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class SerializationTest {

    @Test
    void sakTest() {
        test(enSak(1));
    }
    @Test
    void sakResponsTest() {
        test(sakRespons(2), true);
    }

    @Test
    void faktiskResponsTest() throws Exception {
        testJson(jsonFra("rest/svprespons.json"), true);
    }

    private static void testJson(String json, boolean log) {
        var deser = Arrays.stream(DefaultJsonMapper.fromJson(json, InfotrygdSak[].class)).toList();
        if (log) {
            System.out.println("##");
            System.out.println(json);
            System.out.println("Deserialisert:     " + deser);
        }
    }

    private static void test(InfotrygdSak object) {
        test(List.of(object), true);
    }

    private static void test(List<InfotrygdSak> object, boolean log) {
        String ser = write(object);
        var deser = Arrays.stream(DefaultJsonMapper.fromJson(ser, InfotrygdSak[].class)).toList();
        if (log) {
            System.out.println("##");
            System.out.println("Før serialisering: " + object);
            System.out.println(ser);
            System.out.println("Deserialisert:     " + deser);
        }
        assertEquals(object, deser);
    }

    private static String write(Object object) {
        return DefaultJsonMapper.toJson(object);
    }

    private static InfotrygdSak.Saksnummer saksnummer(int n) {
        return new InfotrygdSak.Saksnummer("B", n);
    }
    private static InfotrygdSak enSak(int n) {
        return new InfotrygdSak(plusDays(n), new InfotrygdSak.SakResultat("FB", "ferdig") , plusDays(-10),
            saksnummer(n), null, new InfotrygdSak.SakType("S", "søknad"), plusDays(1),
            new InfotrygdSak.SakValg("FØ", "fødsel"), new InfotrygdSak.SakUndervalg("  ", "annet"),
            new InfotrygdSak.SakNivå("TK", "Trygdekontor"));
    }

    private static List<InfotrygdSak> saker(int n) {
        return IntStream.range(0, n).boxed().map(SerializationTest::enSak).toList();
    }

    private static LocalDate plusDays(int n) {
        return LocalDate.now().plusDays(n);
    }

    private static List<InfotrygdSak> sakRespons(int n) {
        return saker(n);
    }

    String jsonFra(String fil) throws Exception {
        return new String(readAllBytes(Paths.get(getClass().getClassLoader().getResource(fil).toURI())), UTF_8);
    }

}
