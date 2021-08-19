package no.nav.vedtak.felles.integrasjon.medl2;

import java.time.LocalDate;
import java.util.List;

public interface Medlemskap {

    List<Medlemskapsunntak> finnMedlemsunntak(String aktørId, LocalDate fom, LocalDate tom) throws Exception;

}
