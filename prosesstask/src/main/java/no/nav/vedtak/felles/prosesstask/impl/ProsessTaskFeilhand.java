package no.nav.vedtak.felles.prosesstask.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.KodeverkTabell;
import no.nav.vedtak.felles.prosesstask.spi.ProsessTaskFeilHåndteringParametere;

@Entity(name = "ProsessTaskFeilhand")
@Table(name = "PROSESS_TASK_FEILHAND")
public class ProsessTaskFeilhand extends KodeverkTabell implements ProsessTaskFeilHåndteringParametere {

    @Column(name = "INPUT_VARIABEL1", updatable = false, insertable = false, columnDefinition = "INT8")
    private Integer inputVariabel1;

    @Column(name = "INPUT_VARIABEL2", updatable = false, insertable = false, columnDefinition = "INT8")
    private Integer inputVariabel2;


    @Override
    public Integer getInputVariabel1() {
        return inputVariabel1;
    }

    @Override
    public Integer getInputVariabel2() {
        return inputVariabel2;
    }

}
