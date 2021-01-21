package no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1;

import java.time.LocalDate;

import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker;

public interface InfotrygdSaker {

    Saker getSaker(String fnr, LocalDate fom) throws Exception;

}
