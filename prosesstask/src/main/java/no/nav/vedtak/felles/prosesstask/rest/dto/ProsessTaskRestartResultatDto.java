package no.nav.vedtak.felles.prosesstask.rest.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultatet av asynkron-restart av en eksisterende prosesstask")
public class ProsessTaskRestartResultatDto {

    @NotNull
    private Long prosessTaskId;

    @NotNull
    @Schema(description = "Nåværende status (KLAR)")
    private String prosessTaskStatus;

    @NotNull
    @Schema(description = "Kjøretidspunkt for restart av prosessen")
    private LocalDateTime nesteKjoeretidspunkt;

    public ProsessTaskRestartResultatDto() { // NOSONAR Input-dto, ingen behov for initialisering
    }

    public Long getProsessTaskId() {
        return prosessTaskId;
    }

    public void setProsessTaskId(Long prosessTaskId) {
        this.prosessTaskId = prosessTaskId;
    }

    public String getProsessTaskStatus() {
        return prosessTaskStatus;
    }

    public void setProsessTaskStatus(String prosessTaskStatus) {
        this.prosessTaskStatus = prosessTaskStatus;
    }

    public LocalDateTime getNesteKjoeretidspunkt() {
        return nesteKjoeretidspunkt;
    }

    public void setNesteKjoeretidspunkt(LocalDateTime nesteKjoeretidspunkt) {
        this.nesteKjoeretidspunkt = nesteKjoeretidspunkt;
    }
}
