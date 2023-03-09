package no.nav.vedtak.felles.integrasjon.infotrygd.saker;

import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker;

import java.time.LocalDate;

public interface InfotrygdSaker {

    Saker getSaker(String fnr, LocalDate fom);

}
