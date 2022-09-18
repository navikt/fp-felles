package no.nav.vedtak.felles.integrasjon.medl2;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestCommon;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

/**
 *             Dokumentasjon https://confluence.adeo.no/display/TREG/MEDL+-+Medlemskap+Rest
 *             Swagger: ukjent
 */
@NativeClient
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, endpointProperty = "medl2.rs.url",
    endpointDefault = "https://app.adeo.no/medl2/api/v1/medlemskapsunntak")
@ApplicationScoped
public class MedlemsunntakNativeRestKlient implements Medlemskap {

    public static final String PARAM_FRA_OG_MED = "fraOgMed";
    public static final String PARAM_TIL_OG_MED = "tilOgMed";
    public static final String PARAM_STATUSER = "statuser";
    public static final String PARAM_INKLUDER_SPORINGSINFO = "inkluderSporingsinfo";

    // Fra kodeverk PeriodestatusMedl
    public static final String KODE_PERIODESTATUS_GYLD = "GYLD";
    public static final String KODE_PERIODESTATUS_UAVK = "UAVK";

    private RestClient restKlient;
    private URI endpoint;

    MedlemsunntakNativeRestKlient() {
        // CDI proxyable
    }

    @Inject
    public MedlemsunntakNativeRestKlient(RestClient restKlient) {
        this.restKlient = restKlient;
        this.endpoint = RestConfig.endpointFromAnnotation(MedlemsunntakNativeRestKlient.class);
    }

    @Override
    public List<Medlemskapsunntak> finnMedlemsunntak(String aktørId, LocalDate fom, LocalDate tom) throws Exception {
        var uri = UriBuilder.fromUri(endpoint)
            .queryParam(PARAM_INKLUDER_SPORINGSINFO, String.valueOf(true))
            .queryParam(PARAM_FRA_OG_MED, d2s(fom))
            .queryParam(PARAM_TIL_OG_MED, d2s(tom))
            .queryParam(PARAM_STATUSER, KODE_PERIODESTATUS_GYLD)
            .queryParam(PARAM_STATUSER, KODE_PERIODESTATUS_UAVK);
        var request = RestCommon.get(uri.build(), MedlemsunntakNativeRestKlient.class)
            .otherCallId(NavHeaders.HEADER_NAV_CALL_ID)
            .header(NavHeaders.HEADER_NAV_PERSONIDENT, aktørId);
        var match = restKlient.send(request, Medlemskapsunntak[].class);
        return Arrays.asList(match);
    }

    private static String d2s(LocalDate dato) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(dato);
    }

}
