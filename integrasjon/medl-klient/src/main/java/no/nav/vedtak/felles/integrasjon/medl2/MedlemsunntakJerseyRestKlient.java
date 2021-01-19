package no.nav.vedtak.felles.integrasjon.medl2;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.vedtak.log.mdc.MDCOperations.getCallId;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

/*
 * Dokumentasjon https://confluence.adeo.no/display/FEL/MEDL2
 * Swagger https://app-q1.adeo.no/medl2/swagger-ui.html
 */

@ApplicationScoped
@Named("jersey")
public class MedlemsunntakJerseyRestKlient extends AbstractJerseyOidcRestClient {

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

    private URI endpoint;

    public MedlemsunntakJerseyRestKlient() {
    }

    @Inject
    public MedlemsunntakJerseyRestKlient(@KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.endpoint = endpoint;
    }

    public List<Medlemskapsunntak> finnMedlemsunntak(String aktørId, LocalDate fom, LocalDate tom) throws Exception {
        return client.target(endpoint)
                .queryParam(PARAM_INKLUDER_SPORINGSINFO, "true")
                .queryParam(PARAM_FRA_OG_MED, d2s(fom))
                .queryParam(PARAM_TIL_OG_MED, d2s(tom))
                .queryParam(PARAM_STATUSER, KODE_PERIODESTATUS_GYLD)
                .queryParam(PARAM_STATUSER, KODE_PERIODESTATUS_UAVK)
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .header(HEADER_NAV_CALL_ID, getCallId())
                .header(HEADER_NAV_PERSONIDENT, aktørId)
                .get(Response.class)
                .readEntity(new GenericType<List<Medlemskapsunntak>>() {
                });
    }

    private static String d2s(LocalDate dato) {
        return ISO_LOCAL_DATE.format(dato);
    }
}
