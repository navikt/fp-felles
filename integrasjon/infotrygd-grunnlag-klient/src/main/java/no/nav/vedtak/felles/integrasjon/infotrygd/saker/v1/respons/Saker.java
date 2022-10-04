package no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons;

import static com.fasterxml.jackson.annotation.Nulls.AS_EMPTY;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public record Saker(String info,
                    @JsonProperty("saker") @JsonSetter(nulls = AS_EMPTY) List<Sak> saker,
                    @JsonAlias("apneSakerMedLopendeUtbetaling") @JsonSetter(nulls = AS_EMPTY) List<LøpendeSak> løpendeSaker,
                    AvsluttedeSaker avsluttedeSaker,
                    @JsonProperty("ikkeStartet") @JsonSetter(nulls = AS_EMPTY) List<IkkeStartetSak> ikkeStartet) {

    public record Sak(LocalDate iverksatt, SakResultat resultat, Saksnummer sakId, String status, SakType type, LocalDate vedtatt) {

        @JsonIgnore
        public String getSaksnummer() {
            return sakId().blokk() + nrFra(sakId().nr());
        }

        private static String nrFra(int nr) {
            return nr < 10 ? "0" + nr : String.valueOf(nr);
        }
        public enum SakResultat {
            @JsonEnumDefaultValue
            UKJENT,
            A,
            AK,
            AV,
            DI,
            DT,
            FB,
            FI,
            H,
            HB,
            I,
            IN,
            IS,
            IT,
            MO,
            MT,
            NB,
            O,
            PA,
            R,
            SB,
            TB,
            TH,
            TO,
            UB,
            Ø;
        }

        public enum SakType {
            @JsonEnumDefaultValue
            UKJENT,
            S,
            R,
            K,
            A
        }

        public record Saksnummer(String blokk, int nr) {
        }
    }

    public record AvsluttedeSaker(LocalDate fraOgMed,
                                  @JsonProperty("saker") @JsonSetter(nulls = AS_EMPTY) List<AvsluttetSak> saker) {
        public record AvsluttetSak(LocalDate iverksatt, LocalDate stoppdato,
                                   @JsonProperty("utbetalinger") @JsonSetter(nulls = AS_EMPTY) List<Utbetaling> utbetalinger) {
        }
    }

    public record Utbetaling(int gradering, LocalDate utbetaltFom, LocalDate utbetaltTom) {
    }
    public record IkkeStartetSak(LocalDate iverksatt, LocalDate registrert) {
    }

    public record LøpendeSak(LocalDate iverksatt,
                             @JsonProperty("utbetalinger") @JsonSetter(nulls = AS_EMPTY) List<Utbetaling> utbetalinger) {
    }
}
