package no.nav.vedtak.felles.integrasjon.skjerming;

import java.util.List;

public interface Skjerming {

    boolean erSkjermet(String fnr);

    boolean erNoenSkjermet(List<String> fnr);

}
