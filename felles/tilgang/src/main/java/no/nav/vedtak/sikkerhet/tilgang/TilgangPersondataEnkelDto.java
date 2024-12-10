package no.nav.vedtak.sikkerhet.tilgang;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public record TilgangPersondataEnkelDto(String aktoerId, Person person, Identer identer) {

    public record Person(List<Adressebeskyttelse> adressebeskyttelse) {
    }

    public record Adressebeskyttelse(Gradering gradering) { }

    public record Identer(List<Ident> identer) { }

    public record Ident(String ident, Boolean historisk, IdentGruppe gruppe) { }

    public enum Gradering { STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG, FORTROLIG, @JsonEnumDefaultValue UDEFINERT }

    public enum IdentGruppe { AKTORID, FOLKEREGISTERIDENT, NPID, @JsonEnumDefaultValue UDEFINERT }

    public boolean harStrengAdresseBeskyttelse() {
        return Optional.ofNullable(person()).map(Person::adressebeskyttelse).orElse(List.of()).stream()
            .map(Adressebeskyttelse::gradering)
            .anyMatch(g -> Gradering.STRENGT_FORTROLIG.equals(g) || Gradering.STRENGT_FORTROLIG_UTLAND.equals(g));
    }

    public boolean harAdresseBeskyttelse() {
        return Optional.ofNullable(person()).map(Person::adressebeskyttelse).orElse(List.of()).stream()
            .map(Adressebeskyttelse::gradering)
            .anyMatch(g -> g != null && !Gradering.UDEFINERT.equals(g));
    }

    public String personIdent() {
        return Optional.ofNullable(identer()).map(Identer::identer).orElse(List.of()).stream()
            .filter(i -> IdentGruppe.FOLKEREGISTERIDENT.equals(i.gruppe()))
            .filter(i -> !i.historisk())
            .map(Ident::ident)
            .findFirst().orElse(null);
    }

    public List<String> personIdenter(boolean medHistoriske) {
        return Optional.ofNullable(identer()).map(Identer::identer).orElse(List.of()).stream()
            .filter(i -> IdentGruppe.FOLKEREGISTERIDENT.equals(i.gruppe()))
            .filter(i -> medHistoriske || !i.historisk())
            .map(Ident::ident)
            .toList();
    }

    public String aktørId(boolean medHistoriske) {
        return Optional.ofNullable(identer()).map(Identer::identer).orElse(List.of()).stream()
            .filter(i -> IdentGruppe.AKTORID.equals(i.gruppe()))
            .filter(i -> medHistoriske || !i.historisk())
            .map(Ident::ident)
            .findFirst().orElse(null);
    }

    public List<String> alleIdenter(boolean medHistoriske) {
        return Optional.ofNullable(identer()).map(Identer::identer).orElse(List.of()).stream()
            .filter(i -> medHistoriske || !i.historisk())
            .map(Ident::ident)
            .toList();
    }
}
