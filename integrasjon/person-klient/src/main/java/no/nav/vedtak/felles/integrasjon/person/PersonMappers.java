package no.nav.vedtak.felles.integrasjon.person;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;

import no.nav.pdl.Adressebeskyttelse;
import no.nav.pdl.AdressebeskyttelseGradering;
import no.nav.pdl.Doedsfall;
import no.nav.pdl.Foedselsdato;
import no.nav.pdl.Folkeregisteridentifikator;
import no.nav.pdl.Folkeregisterpersonstatus;
import no.nav.pdl.Kjoenn;
import no.nav.pdl.KjoennType;
import no.nav.pdl.Navn;
import no.nav.pdl.Person;
import no.nav.pdl.Personnavn;

public class PersonMappers {

    private PersonMappers() {
    }

    // Må ha projection med folkeregisteridentifikator  + status
    public static boolean harIdentifikator(Person person) {
        return harIdentifikator(person.getFolkeregisteridentifikator());
    }

    public static boolean manglerIdentifikator(Person person) {
        return !harIdentifikator(person.getFolkeregisteridentifikator());
    }

    private static boolean harIdentifikator(Collection<Folkeregisteridentifikator> folkeregisteridentifikator) {
        return folkeregisteridentifikator.stream()
            .map(Folkeregisteridentifikator::getStatus)
            .anyMatch("I_BRUK"::equals);
    }

    // Må ha projection med kjoenn + kjoenn
    public static KjoennType mapKjønn(Person person) {
        return person.getKjoenn().stream()
            .map(Kjoenn::getKjoenn)
            .filter(k -> k != null && !KjoennType.UKJENT.equals(k))
            .findFirst().orElse(KjoennType.UKJENT);
    }

    // Må ha projection med navn + for/mellom/etternavn
    public static Optional<String> mapNavn(Person person) {
        return person.getNavn().stream()
            .map(PersonMappers::mapNavn)
            .findFirst();
    }

    public static String mapNavn(Navn navn) {
        return navn.getFornavn() + leftPad(navn.getMellomnavn()) + leftPad(navn.getEtternavn());
    }

    public static String mapNavn(Personnavn navn) {
        return navn.getFornavn() + leftPad(navn.getMellomnavn()) + leftPad(navn.getEtternavn());
    }

    public static String leftPad(String navn) {
        return Optional.ofNullable(navn).map(n -> " " + navn).orElse("");
    }

    // Må ha projection med foedselsdato  + foedselsdato
    public static Optional<LocalDate> mapFødselsdato(Person person) {
        return person.getFoedselsdato().stream()
            .map(Foedselsdato::getFoedselsdato)
            .map(PersonMappers::mapDato)
            .flatMap(Optional::stream)
            .findFirst();
    }

    // Må ha projection med doedsfall  + doedsdato
    public static Optional<LocalDate> mapDødsdato(Person person) {
        return person.getDoedsfall().stream()
            .map(Doedsfall::getDoedsdato)
            .map(PersonMappers::mapDato)
            .flatMap(Optional::stream)
            .findFirst();
    }

    public static Optional<LocalDate> mapDato(String dato) {
        return Optional.ofNullable(dato).map(d -> LocalDate.parse(d, DateTimeFormatter.ISO_LOCAL_DATE));
    }

    // Må ha projection med adressebeskyttelse + gradering
    public static AdressebeskyttelseGradering mapAdressebeskyttelse(Person person) {
        return person.getAdressebeskyttelse().stream()
            .map(Adressebeskyttelse::getGradering)
            .filter(g -> g != null && !AdressebeskyttelseGradering.UGRADERT.equals(g))
            .findFirst().orElse(AdressebeskyttelseGradering.UGRADERT);
    }

    // Må ha projection med folkeregisterpersonstatus + status
    public static boolean harStatusUtflyttet(Person person) {
        return person.getFolkeregisterpersonstatus().stream()
            .map(Folkeregisterpersonstatus::getStatus)
            .anyMatch("utflyttet"::equals);
    }

}
