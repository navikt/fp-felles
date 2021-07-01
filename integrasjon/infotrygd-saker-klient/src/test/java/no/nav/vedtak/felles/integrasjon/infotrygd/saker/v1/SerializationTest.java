package no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper.MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker;

class SerializationTest {

    @Test
    void saksnummerTest() throws Exception {
        test(saksnummer(42));
    }

    @Test
    void sakTest() throws Exception {
        test(enSak(1));
    }

    @Test
    void utbetalingTest() throws Exception {
        test(utbetaling(0));
    }

    @Test
    void åpenSakTest() throws Exception {
        test(åpenSak(4));
    }

    @Test
    void avsluttedeSakerTest() throws Exception {
        test(avsluttedeSaker(4));
    }

    @Test
    void avsluttedeSakTest() throws Exception {
        test(enAvsluttetSak(4));
    }

    @Test
    void sakResponsTest() throws Exception {
        test(sakRespons(2));
    }

    @Test
    void faktiskResponsTest() throws Exception {
        testJson(jsonFra("rest/svprespons.json"), Saker.class);
    }

    private static void testJson(String json, Class<?> clazz) throws Exception {
        testJson(json, clazz, true);
    }

    private static void testJson(String json, Class<?> clazz, boolean log) throws Exception {
        var deser = MAPPER.readValue(json, clazz);
        if (log) {
            System.out.println("##");
            System.out.println(json);
            System.out.println("Deserialisert:     " + deser);
        }
    }

    private static void test(Object object) throws IOException {
        test(object, true);
    }

    private static void test(Object object, boolean log) throws IOException {
        String ser = write(object);
        var deser = MAPPER.readValue(ser, object.getClass());
        if (log) {
            System.out.println("##");
            System.out.println("Før serialisering: " + object);
            System.out.println(ser);
            System.out.println("Deserialisert:     " + deser);
        }
        assertEquals(object, deser);
    }

    private static String write(Object object) throws JsonProcessingException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    private static Saker.Sak.Saksnummer saksnummer(int n) {
        return new Saker.Sak.Saksnummer("B", n);
    }

    private static Saker.Sak enSak(int n) {
        return new Saker.Sak(plusDays(n), Saker.Sak.SakResultat.FB, saksnummer(n), "FI", Saker.Sak.SakType.S, plusDays(1));
    }

    private static Saker.LøpendeSak åpenSak(int n) {
        return new Saker.LøpendeSak(plusDays(n), utbetalinger(n));
    }

    private static Saker.AvsluttedeSaker avsluttedeSaker(int n) {
        return new Saker.AvsluttedeSaker(plusDays(n), alleAvsluttedeSaker(n));
    }

    private static Saker.AvsluttedeSaker.AvsluttetSak enAvsluttetSak(int n) {
        return new Saker.AvsluttedeSaker.AvsluttetSak(plusDays(n), plusDays(n + 1), utbetalinger(n));
    }

    private static Saker.IkkeStartetSak enIkkeStartetSak(int n) {
        return new Saker.IkkeStartetSak(plusDays(n), plusDays(n + 1));
    }

    private static List<Saker.AvsluttedeSaker.AvsluttetSak> alleAvsluttedeSaker(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::enAvsluttetSak)
                .collect(toList());
    }

    private static List<Saker.Utbetaling> utbetalinger(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::utbetaling)
                .collect(toList());
    }

    private static List<Saker.LøpendeSak> åpneSaker(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::åpenSak)
                .collect(toList());
    }

    private static List<Saker.Sak> saker(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::enSak)
                .collect(toList());
    }

    private static List<Saker.IkkeStartetSak> ikkeStartedeSaker(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::enIkkeStartetSak)
                .collect(toList());
    }

    private static Saker.Utbetaling utbetaling(int n) {
        return new Saker.Utbetaling(80, plusDays(n), plusDays(n + 1));
    }

    private static LocalDate plusDays(int n) {
        return LocalDate.now().plusDays(n);
    }

    private static Saker sakRespons(int n) {
        return new Saker("hello", saker(n), åpneSaker(n), avsluttedeSaker(n), ikkeStartedeSaker(n));
    }

    String jsonFra(String fil) throws Exception {
        return new String(readAllBytes(Paths.get(getClass().getClassLoader().getResource(fil).toURI())), UTF_8);
    }

}
