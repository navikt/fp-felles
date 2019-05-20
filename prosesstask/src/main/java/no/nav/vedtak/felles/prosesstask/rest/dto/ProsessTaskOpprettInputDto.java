package no.nav.vedtak.felles.prosesstask.rest.dto;

import java.util.Properties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class ProsessTaskOpprettInputDto implements AbacDto {

    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = "^[a-zA-ZæøåÆØÅ0-9_.\\-]*$")
    private String taskType;

    @NotNull
    @Valid
    private Properties taskParametre = new Properties();

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Properties getTaskParametre() {
        return taskParametre;
    }

    public void setTaskParametre(Properties taskParametre) {
        this.taskParametre = taskParametre;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
