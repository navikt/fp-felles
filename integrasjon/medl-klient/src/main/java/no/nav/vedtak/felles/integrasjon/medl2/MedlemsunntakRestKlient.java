package no.nav.vedtak.felles.integrasjon.medl2;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.log.mdc.MDCOperations;

/**
 *
 * @deprecated Bruk {@link MedlemsunntakJerseyRestKlient}
 *
 *             Dokumentasjon https://confluence.adeo.no/display/FEL/MEDL2
 *             Swagger https://app-q1.adeo.no/medl2/swagger-ui.html
 */

@Deprecated
@ApplicationScoped
public class MedlemsunntakRestKlient implements Medlemsskap {

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

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public MedlemsunntakRestKlient() {
    }

    @Inject
    public MedlemsunntakRestKlient(OidcRestClient oidcRestClient,
            @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpoint = endpoint;
    }

    @Override
    public List<Medlemskapsunntak> finnMedlemsunntak(String aktørId, LocalDate fom, LocalDate tom) throws Exception {
        URIBuilder builder = new URIBuilder(this.endpoint)
                .addParameter(PARAM_INKLUDER_SPORINGSINFO, String.valueOf(true))
                .addParameter(PARAM_FRA_OG_MED, d2s(fom))
                .addParameter(PARAM_TIL_OG_MED, d2s(tom))
                .addParameter(PARAM_STATUSER, KODE_PERIODESTATUS_GYLD)
                .addParameter(PARAM_STATUSER, KODE_PERIODESTATUS_UAVK);
        var match = this.oidcRestClient.get(builder.build(), lagHeader(aktørId), Medlemskapsunntak[].class);
        return Arrays.asList(match);
    }

    private static Set<Header> lagHeader(String aktørId) {
        return Set.of(new BasicHeader(HEADER_NAV_CALL_ID, MDCOperations.getCallId()),
                new BasicHeader(HEADER_NAV_PERSONIDENT, aktørId));
    }

    private static String d2s(LocalDate dato) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(dato);
    }

}
