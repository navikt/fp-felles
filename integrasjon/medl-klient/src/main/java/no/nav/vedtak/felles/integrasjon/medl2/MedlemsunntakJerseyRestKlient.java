package no.nav.vedtak.felles.integrasjon.medl2;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

/*
 * Dokumentasjon https://confluence.adeo.no/display/FEL/MEDL2
 * Swagger https://app-q1.adeo.no/medl2/swagger-ui.html
 */

@Dependent
@Jersey
public class MedlemsunntakJerseyRestKlient extends AbstractJerseyOidcRestClient implements Medlemsskap {

    private static final String ENDPOINT_KEY = "medl2.rs.url";
    private static final String DEFAULT_URI = "https://app.adeo.no/medl2/api/v1/medlemskapsunntak";

    public static final String HEADER_NAV_PERSONIDENT = "Nav-Personident";
    public static final String PARAM_FRA_OG_MED = "fraOgMed";
    public static final String PARAM_TIL_OG_MED = "tilOgMed";
    public static final String PARAM_STATUSER = "statuser";
    public static final String PARAM_INKLUDER_SPORINGSINFO = "inkluderSporingsinfo";

    // Fra kodeverk PeriodestatusMedl
    public static final String KODE_PERIODESTATUS_GYLD = "GYLD";
    public static final String KODE_PERIODESTATUS_UAVK = "UAVK";
    private static final Logger LOG = LoggerFactory.getLogger(MedlemsunntakJerseyRestKlient.class);

    private final URI endpoint;

    @Inject
    public MedlemsunntakJerseyRestKlient(@KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public List<Medlemskapsunntak> finnMedlemsunntak(String aktørId, LocalDate fom, LocalDate tom) throws Exception {
            var target = client.target(endpoint)
                    .queryParam(PARAM_INKLUDER_SPORINGSINFO, "true")
                    .queryParam(PARAM_FRA_OG_MED, d2s(fom))
                    .queryParam(PARAM_TIL_OG_MED, d2s(tom))
                    .queryParam(PARAM_STATUSER, KODE_PERIODESTATUS_GYLD)
                    .queryParam(PARAM_STATUSER, KODE_PERIODESTATUS_UAVK);
            LOG.trace("Henter unntak fra {}", target.getUri());
            var res = invoke(target
                    .request()
                    .accept(APPLICATION_JSON_TYPE)
                    .header(HEADER_NAV_PERSONIDENT, aktørId)
                    .buildGet(),
                    new GenericType<List<Medlemskapsunntak>>() {
                    });
            LOG.info("Hentet unntak OK");
            return res;
    }

    private static String d2s(LocalDate dato) {
        return ISO_LOCAL_DATE.format(dato);
    }
}
