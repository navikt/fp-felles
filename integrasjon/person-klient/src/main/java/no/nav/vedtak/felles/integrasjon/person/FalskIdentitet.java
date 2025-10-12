package no.nav.vedtak.felles.integrasjon.person;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.pdl.FalskIdentitetIdentifiserendeInformasjonResponseProjection;
import no.nav.pdl.FalskIdentitetResponseProjection;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.KjoennType;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.pdl.PersonnavnResponseProjection;
import no.nav.vedtak.konfig.Tid;

public class FalskIdentitet {

    private static final Logger LOG = LoggerFactory.getLogger(FalskIdentitet.class);

    private static final String LAND_UKJENT = "XUK";
    private static final String LAND_UKJENT2 = "XXX";
    private static final String STATUS_OPPHØRT = "opphoert";
    private static final List<String> UKJENT_LAND = List.of(LAND_UKJENT);

    public record Informasjon(String navn, LocalDate fødselsdato, List<String> statsborgerskap,
                              KjoennType kjønn, String personstatus) {
    }

    private FalskIdentitet() {
    }

    public static Optional<Informasjon> finnFalskIdentitet(String ident, Persondata klient) {
        try {
            var query = new HentPersonQueryRequest();
            query.setIdent(ident);
            var projection = new PersonResponseProjection()
                .falskIdentitet(new FalskIdentitetResponseProjection().erFalsk().rettIdentitetErUkjent().rettIdentitetVedIdentifikasjonsnummer()
                    .rettIdentitetVedOpplysninger(new FalskIdentitetIdentifiserendeInformasjonResponseProjection().kjoenn().foedselsdato()
                        .personnavn(new PersonnavnResponseProjection().fornavn().mellomnavn().etternavn()).statsborgerskap()));

            var falskIdentitetPerson = klient.hentPerson(Persondata.Ytelse.FORELDREPENGER, query, projection);
            return lagFalskIdentitetInformasjon(falskIdentitetPerson);
        } catch (Exception e) {
            LOG.warn("Falsk identitet: Feil ved oppslag", e);
            return Optional.empty();
        }
    }

    private static Optional<Informasjon> lagFalskIdentitetInformasjon(Person falskIdentitetPerson) {
        if (falskIdentitetPerson.getFalskIdentitet() != null && falskIdentitetPerson.getFalskIdentitet().getErFalsk()) {
            var falskIdentitet = falskIdentitetPerson.getFalskIdentitet();
            // Falsk Identitet skal mangle personidentifikator, ha opphørt personstatus og kanskje informasjon i falskIdentitet
            if (Objects.equals(falskIdentitet.getRettIdentitetErUkjent(), Boolean.TRUE)) {
                return Optional.of(new Informasjon( "Falsk Identitet", Tid.TIDENES_BEGYNNELSE, UKJENT_LAND, KjoennType.UKJENT, STATUS_OPPHØRT));
            } else if (falskIdentitet.getRettIdentitetVedIdentifikasjonsnummer() != null) {
                LOG.warn("Falsk identitet: rettIdentitetVedIdentifikasjonsnummer {}", falskIdentitet.getRettIdentitetVedIdentifikasjonsnummer());
                throw new IllegalStateException("Falsk identitet: rettIdentitetVedIdentifikasjonsnummer finnes");
            } else if (falskIdentitet.getRettIdentitetVedOpplysninger() != null) {
                var falskIdentitetInfo = falskIdentitet.getRettIdentitetVedOpplysninger();
                var kjønn = Optional.ofNullable(falskIdentitetInfo.getKjoenn()).orElse(KjoennType.UKJENT);
                var navn = PersonMappers.mapNavn(falskIdentitetInfo.getPersonnavn());
                var fødselsdato = Optional.ofNullable(falskIdentitetInfo.getFoedselsdato()).
                    flatMap(PersonMappers::mapDato).orElse(Tid.TIDENES_BEGYNNELSE);
                var statsborgerskap = falskIdentitetInfo.getStatsborgerskap().stream()
                    .filter(l -> !LAND_UKJENT.equals(l) && !LAND_UKJENT2.equals(l))
                    .toList();
                var brukStatsborgerskap = statsborgerskap.isEmpty() ? UKJENT_LAND : statsborgerskap;
                return Optional.of(new Informasjon(navn, fødselsdato, brukStatsborgerskap, kjønn, STATUS_OPPHØRT));
            }
        }
        return Optional.empty();
    }

}
