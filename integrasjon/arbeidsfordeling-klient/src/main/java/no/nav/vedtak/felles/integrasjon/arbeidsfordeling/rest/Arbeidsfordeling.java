package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import java.util.List;

public interface Arbeidsfordeling {

    List<ArbeidsfordelingResponse> hentAlleAktiveEnheter(ArbeidsfordelingRequest request);

    List<ArbeidsfordelingResponse> finnEnhet(ArbeidsfordelingRequest request);

}
