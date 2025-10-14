package no.nav.vedtak.felles.integrasjon.person;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class PersonMappersTest {

    private Person person;

    @BeforeEach
    void setUp() {
        // Service setup
        person = new Person();
    }

    @Test
    void map_kjønn() {
        person.setKjoenn(List.of(new Kjoenn(KjoennType.MANN, null, null)));
        assertThat(PersonMappers.mapKjønn(person)).isEqualTo(KjoennType.MANN);

        person.setKjoenn(List.of(new Kjoenn(KjoennType.KVINNE, null, null),
            new Kjoenn(KjoennType.UKJENT, null, null)));
        assertThat(PersonMappers.mapKjønn(person)).isEqualTo(KjoennType.KVINNE);

        person.setKjoenn(List.of());
        assertThat(PersonMappers.mapKjønn(person)).isEqualTo(KjoennType.UKJENT);
    }

    @Test
    void map_gradering() {
        person.setAdressebeskyttelse(List.of(new Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG, null, null)));
        assertThat(PersonMappers.mapAdressebeskyttelse(person)).isEqualTo(AdressebeskyttelseGradering.FORTROLIG);

        person.setAdressebeskyttelse(List.of(new Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG, null, null),
            new Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT, null, null)));
        assertThat(PersonMappers.mapAdressebeskyttelse(person)).isEqualTo(AdressebeskyttelseGradering.STRENGT_FORTROLIG);

        person.setAdressebeskyttelse(List.of());
        assertThat(PersonMappers.mapAdressebeskyttelse(person)).isEqualTo(AdressebeskyttelseGradering.UGRADERT);
    }

    @Test
    void sjekk_identifikator() {
        person.setFolkeregisteridentifikator(List.of(new Folkeregisteridentifikator("12345678901", "I_BRUK", "FNR", null, null)));
        assertThat(PersonMappers.manglerIdentifikator(person)).isFalse();

        person.setFolkeregisteridentifikator(List.of(new Folkeregisteridentifikator("12345678901", "OPPHOERT", "FNR", null, null)));
        assertThat(PersonMappers.manglerIdentifikator(person)).isTrue();

        person.setFolkeregisteridentifikator(List.of(new Folkeregisteridentifikator("12345678901", "I_BRUK", "FNR", null, null),
            new Folkeregisteridentifikator("52345678901", "OPPHOERT", "FNR", null, null)));
        assertThat(PersonMappers.manglerIdentifikator(person)).isFalse();

        person.setFolkeregisteridentifikator(List.of());
        assertThat(PersonMappers.manglerIdentifikator(person)).isTrue();
    }

    @Test
    void sjekk_utflyttet() {
        person.setFolkeregisterpersonstatus(List.of(new Folkeregisterpersonstatus("utflyttet", "ikkeBosatt", null, null)));
        assertThat(PersonMappers.harStatusUtflyttet(person)).isTrue();

        person.setFolkeregisterpersonstatus(List.of(new Folkeregisterpersonstatus("bosatt", "bosatt", null, null)));
        assertThat(PersonMappers.harStatusUtflyttet(person)).isFalse();

        person.setAdressebeskyttelse(List.of());
        assertThat(PersonMappers.harStatusUtflyttet(person)).isFalse();
    }

    @Test
    void map_fødselsdato() {
        var idag = LocalDate.now();
        person.setFoedselsdato(List.of(new Foedselsdato(idag.toString(), idag.getYear(), null, null)));
        assertThat(PersonMappers.mapFødselsdato(person)).isPresent().hasValueSatisfying(f -> assertThat(f).isEqualTo(idag));

        person.setFoedselsdato(List.of());
        assertThat(PersonMappers.mapFødselsdato(person)).isEmpty();
    }

    @Test
    void map_dødsdato() {
        var idag = LocalDate.now();
        person.setDoedsfall(List.of(new Doedsfall(idag.toString(), null, null)));
        assertThat(PersonMappers.mapDødsdato(person)).isPresent().hasValueSatisfying(f -> assertThat(f).isEqualTo(idag));

        person.setDoedsfall(List.of());
        assertThat(PersonMappers.mapDødsdato(person)).isEmpty();
    }

    @Test
    void map_navn() {
        person.setNavn(List.of(new Navn("Ola", null, "Nordmann", "", null, "", null, null)));
        assertThat(PersonMappers.mapNavn(person)).isPresent().hasValueSatisfying(n -> assertThat(n).isEqualTo("Ola Nordmann"));

        person.setNavn(List.of());
        assertThat(PersonMappers.mapNavn(person)).isEmpty();

        assertThat(PersonMappers.mapNavn(new Personnavn("Kari", null, "Nordmann"))).isEqualTo("Kari Nordmann");
    }

}
