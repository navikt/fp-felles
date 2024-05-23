package no.nav.vedtak.felles.integrasjon.spokelse;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

//Flytt ut til brukere @RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC,
//    endpointProperty = "SPOKELSE_GRUNNLAG_URL", endpointDefault = "http://spokelse.tbd/grunnlag",
//    scopesProperty = "SPOKELSE_GRUNNLAG_SCOPES", scopesDefault = "api://prod-fss.tbd.spokelse/.default")
public abstract class AbstractSpøkelseKlient implements Spøkelse {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSpøkelseKlient.class);

    private final RestClient restKlient;
    private final RestConfig restConfig;

    protected AbstractSpøkelseKlient() {
        this(RestClient.client());
    }

    protected AbstractSpøkelseKlient(RestClient client) {
        this.restKlient = client;
        this.restConfig = RestConfig.forClient(this.getClass());
        if (!restConfig.tokenConfig().isAzureAD()) {
            throw new IllegalArgumentException("Utviklerfeil - klienten må være annoptert med tokenConfig Azure CC");
        }
    }


    @Override
    public List<SykepengeVedtak> hentGrunnlag(String fnr) {
        return hentGrunnlag(fnr, null);
    }

    @Override
    public List<SykepengeVedtak> hentGrunnlag(String fnr, LocalDate fom) {
        return hentGrunnlag(fnr, fom, Duration.ofSeconds(30));
    }

    @Override
    public List<SykepengeVedtak> hentGrunnlag(String fnr, LocalDate fom, Duration timeout) {
        if (fnr == null || fnr.isEmpty()) {
            throw new IllegalArgumentException("Ikke angitt fnr");
        }
        try {
            var request = new GrunnlagRequest(fnr, fom);
            var rrequest = RestRequest.newPOSTJson(request, restConfig.endpoint(), restConfig);
            var resultat = restKlient.send(rrequest, SykepengeVedtak[].class);
            return Arrays.asList(resultat);
        } catch (Exception e) {
            throw new TekniskException("FP-180126",
                String.format("SPokelse %s gir feil, ta opp med team sykepenger.", restConfig.endpoint().toString()), e);
        }
    }

    @Override
    public List<SykepengeVedtak> hentGrunnlagFailSoft(String fnr) {
        return hentGrunnlagFailSoft(fnr, null);
    }

    @Override
    public List<SykepengeVedtak> hentGrunnlagFailSoft(String fnr, LocalDate fom) {
        try {
            return hentGrunnlag(fnr, fom);
        } catch (Exception e) {
            LOG.info("SPokelse felles: feil ved oppslag mot {}, returnerer ingen grunnlag", restConfig.endpoint(), e);
            return List.of();
        }
    }

    public record GrunnlagRequest(String fodselsnummer, LocalDate fom) { }
}
