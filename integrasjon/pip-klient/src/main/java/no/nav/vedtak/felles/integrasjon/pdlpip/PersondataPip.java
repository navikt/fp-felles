package no.nav.vedtak.felles.integrasjon.pdlpip;

import java.util.List;
import java.util.Map;

public interface PersondataPip {

    PersondataPipDto hentTilgangPersondata(String ident);

    Map<String, PersondataPipDto> hentTilgangPersondataBolk(List<String> identer);

}
