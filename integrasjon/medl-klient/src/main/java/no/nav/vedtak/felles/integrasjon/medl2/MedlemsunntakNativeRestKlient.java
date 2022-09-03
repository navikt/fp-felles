package no.nav.vedtak.felles.integrasjon.medl2;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.NativeKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

/**
 *             Dokumentasjon https://confluence.adeo.no/display/TREG/MEDL+-+Medlemskap+Rest
 *             Swagger: ukjent
 */
@NativeKlient
@ApplicationScoped
public class MedlemsunntakNativeRestKlient implements Medlemskap {

    private static final String ENDPOINT_KEY = "medl2.rs.url";
    private static final String DEFAULT_URI = "https://app.adeo.no/medl2/api/v1/medlemskapsunntak";

    public static final String HEADER_NAV_CALL_ID = "Nav-Call-Id";
    public static final String HEADER_NAV_PERSONIDENT = "Nav-Personident";
    public static final String PARAM_FRA_OG_MED = "fraOgMed";
    public static final String PARAM_TIL_OG_MED = "tilOgMed";
    public static final String PARAM_STATUSER = "statuser";
    public static final String PARAM_INKLUDER_SPORINGSINFO = "inkluderSporingsinfo";

    // Fra kodeverk PeriodestatusMedl
    public static final String KODE_PERIODESTATUS_GYLD = "GYLD";
    public static final String KODE_PERIODESTATUS_UAVK = "UAVK";

    private RestKlient restKlient;
    private URI endpoint;

    MedlemsunntakNativeRestKlient() {
        // CDI proxyable
    }

    @Inject
    public MedlemsunntakNativeRestKlient(RestKlient restKlient,
                                         @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.restKlient = restKlient;
        this.endpoint = endpoint;
    }

    @Override
    public List<Medlemskapsunntak> finnMedlemsunntak(String aktørId, LocalDate fom, LocalDate tom) throws Exception {
        var uri = UriBuilder.fromUri(endpoint)
            .queryParam(PARAM_INKLUDER_SPORINGSINFO, String.valueOf(true))
            .queryParam(PARAM_FRA_OG_MED, d2s(fom))
            .queryParam(PARAM_TIL_OG_MED, d2s(tom))
            .queryParam(PARAM_STATUSER, KODE_PERIODESTATUS_GYLD)
            .queryParam(PARAM_STATUSER, KODE_PERIODESTATUS_UAVK);
        var request = RestRequest.builder(SikkerhetContext.BRUKER)
            .header(HEADER_NAV_CALL_ID, MDCOperations.getCallId())
            .header(HEADER_NAV_PERSONIDENT, aktørId)
            .uri(uri.build())
            .GET()
            .build();
        var match = restKlient.send(request, Medlemskapsunntak[].class);
        return Arrays.asList(match);
    }

    private static String d2s(LocalDate dato) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(dato);
    }

}
