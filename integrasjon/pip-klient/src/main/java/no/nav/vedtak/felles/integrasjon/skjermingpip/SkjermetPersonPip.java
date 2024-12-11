package no.nav.vedtak.felles.integrasjon.skjermingpip;

import java.util.List;

public interface SkjermetPersonPip {

    boolean erSkjermet(String fnr);

    boolean erNoenSkjermet(List<String> fnr);

}
