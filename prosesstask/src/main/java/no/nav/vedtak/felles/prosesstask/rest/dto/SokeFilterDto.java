package no.nav.vedtak.felles.prosesstask.rest.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

@Schema
public class SokeFilterDto implements AbacDto {

    @Size(max = 10)
    @Valid
    private List<ProsessTaskStatusDto> prosessTaskStatuser = new ArrayList<>();
    private LocalDateTime sisteKjoeretidspunktFraOgMed = LocalDateTime.now().minusHours(24);
    private LocalDateTime sisteKjoeretidspunktTilOgMed = LocalDateTime.now();

    public SokeFilterDto() {
    }

    @Schema(description = "Angi liste over prosesstask-statuser som skal søkes på, blant KLAR, FERDIG, VENTER_SVAR, SUSPENDERT, eller FEILET")
    public List<ProsessTaskStatusDto> getProsessTaskStatuser() {
        return prosessTaskStatuser;
    }

    public void setProsessTaskStatuser(List<ProsessTaskStatusDto> prosessTaskStatuser) {
        this.prosessTaskStatuser = prosessTaskStatuser;
    }

    @Schema(description = "Søker etter prosesstask med siste kjøring fra og med dette tidspunktet")
    public LocalDateTime getSisteKjoeretidspunktFraOgMed() {
        return sisteKjoeretidspunktFraOgMed;
    }

    public void setSisteKjoeretidspunktFraOgMed(LocalDateTime sisteKjoeretidspunktFraOgMed) {
        this.sisteKjoeretidspunktFraOgMed = sisteKjoeretidspunktFraOgMed;
    }

    @Schema(description = "Søker etter prosesstask med siste kjøring til og med dette tidspunktet")
    public LocalDateTime getSisteKjoeretidspunktTilOgMed() {
        return sisteKjoeretidspunktTilOgMed;
    }

    public void setSisteKjoeretidspunktTilOgMed(LocalDateTime sisteKjoeretidspunktTilOgMed) {
        this.sisteKjoeretidspunktTilOgMed = sisteKjoeretidspunktTilOgMed;
    }

    public Sporingsdata lagSporingsloggData(String action) {
        Sporingsdata sporingsdata = Sporingsdata.opprett(action);
        sporingsdata.leggTilId(ProsessTaskSporingsloggId.PROSESS_TASK_STATUS, prosessTaskStatuser.stream().map(ProsessTaskStatusDto::getProsessTaskStatusName).collect(Collectors.joining(",")));
        sporingsdata.leggTilId(ProsessTaskSporingsloggId.PROSESS_TASK_KJORETIDSINTERVALL, String.format("%s-%s", sisteKjoeretidspunktFraOgMed, sisteKjoeretidspunktTilOgMed));
        return sporingsdata;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett(); //denne er tom, ProsessTask-API har i praksis rollebasert tilgangskontroll
    }
}
