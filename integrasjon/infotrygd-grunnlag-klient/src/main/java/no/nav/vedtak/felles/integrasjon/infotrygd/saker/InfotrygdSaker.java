package no.nav.vedtak.felles.integrasjon.infotrygd.saker;

import java.time.LocalDate;

import no.nav.vedtak.felles.integrasjon.dokarkiv.respons.Saker;

public interface InfotrygdSaker {

    Saker getSaker(String fnr, LocalDate fom);

}
