package no.nav.vedtak.felles.integrasjon.skjermingpip;

import java.util.List;
import java.util.Map;

public interface SkjermetPersonPip {

    boolean erSkjermet(String fnr);

    Map<String, Boolean> erSkjermet(List<String> fnr);

    boolean erNoenSkjermet(List<String> fnr);

}
