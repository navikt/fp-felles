package no.nav.vedtak.sikkerhet.tilgang;

import java.util.List;
import java.util.Map;

public interface TilgangPersondata {

    // ident er aktørId eller personident
    TilgangPersondataDto hentTilgangPersondata(String ident);

    // identer er aktørId eller personident. Respons er map fra personident til responsobjekt
    Map<String, TilgangPersondataDto> hentTilgangPersondataBolk(List<String> identer);

}
