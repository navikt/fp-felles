package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Grunnlag(Status status,
                       Tema tema,
                       Prosent dekningsgrad,
                       @JsonProperty("fødselsdatoBarn") @JsonAlias("foedselsdatoBarn") LocalDate fødselsdatoBarn,
                       @JsonProperty("kategori") @JsonAlias("arbeidskategori") Arbeidskategori kategori,
                       List<Arbeidsforhold> arbeidsforhold,
                       Periode periode,
                       Behandlingstema behandlingstema,
                       LocalDate identdato,
                       LocalDate iverksatt,
                       @JsonProperty("opphørFom") @JsonAlias("opphoerFom") LocalDate opphørFom,
                       Integer gradering,
                       LocalDate opprinneligIdentdato,
                       LocalDate registrert,
                       String saksbehandlerId,
                       List<Vedtak> vedtak) {

}
