package no.nav.vedtak.felles.prosesstask.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.KodeverkTabell;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;

/**
 * Konfigurasjon for en type task
 */
@Entity(name = "ProsessTaskType")
@Table(name = "PROSESS_TASK_TYPE")
public class ProsessTaskType extends KodeverkTabell {

    @Column(name = "feil_maks_forsoek", updatable = false, insertable = false, nullable = false)
    private int maksForsøk = 1;

    @Column(name = "feil_sek_mellom_forsoek", updatable = false, insertable = false, nullable = false)
    private int sekundeFørNesteForsøk = 1;

    @ManyToOne
    @JoinColumn(name = "feilhandtering_algoritme", updatable = false, insertable = false)
    private ProsessTaskFeilhand feilhåndteringAlgoritme;

    @Column(name = "cron_expression")
    private String cronExpression;

    public ProsessTaskType() {
        // for hibernate
    }

    public ProsessTaskType(String taskType) {
        super(taskType);
    }

    ProsessTaskType(String taskType, String cronExpression) {
        super(taskType);
        this.cronExpression = cronExpression;
    }

    public int getMaksForsøk() {
        return maksForsøk;
    }

    public int getSekundeFørNesteForsøk() {
        return sekundeFørNesteForsøk;
    }

    public ProsessTaskFeilhand getFeilhåndteringAlgoritme() {
        return feilhåndteringAlgoritme;
    }

    String getCronExpression() {
        return cronExpression;
    }

    boolean getErGjentagende() {
        return cronExpression != null;
    }

    public ProsessTaskTypeInfo tilProsessTaskTypeInfo() {
        return new ProsessTaskTypeInfo(getKode(), getMaksForsøk());
    }

}
