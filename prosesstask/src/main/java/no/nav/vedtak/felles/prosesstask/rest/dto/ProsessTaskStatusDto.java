package no.nav.vedtak.felles.prosesstask.rest.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import no.nav.vedtak.util.InputValideringRegex;

@Schema
public class ProsessTaskStatusDto {
    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String prosessTaskStatusName;

    public ProsessTaskStatusDto() {
    }

    public ProsessTaskStatusDto(String prosessTaskStatusName) {
        this.prosessTaskStatusName = prosessTaskStatusName;
    }

    @Schema(required = true, description = "Navn på prosesstask-status")
    public String getProsessTaskStatusName() {
        return prosessTaskStatusName;
    }
}
