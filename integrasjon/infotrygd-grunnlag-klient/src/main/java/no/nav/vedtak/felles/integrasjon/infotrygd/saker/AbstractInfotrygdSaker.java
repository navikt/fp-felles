package no.nav.vedtak.felles.integrasjon.infotrygd.saker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.GrunnlagRequest;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.InfotrygdSak;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

// Extend og annoter med endpoint, evt default. tokenConfig kan settes til TokenFlow.AZURE_CC
public abstract class AbstractInfotrygdSaker implements InfotrygdSaker {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractInfotrygdSaker.class);

    private final RestClient restClient;
    private final RestConfig restConfig;

    protected AbstractInfotrygdSaker() {
        this(RestClient.client());
    }

    protected AbstractInfotrygdSaker(RestClient client) {
        this.restClient = client;
        this.restConfig = RestConfig.forClient(this.getClass());
    }

    @Override
    public List<InfotrygdSak> hentSaker(GrunnlagRequest request) {
        try {
            var rrequest = RestRequest.newPOSTJson(request, restConfig.endpoint(), restConfig);
            var resultat = restClient.send(rrequest, InfotrygdSak[].class);
            return Arrays.asList(resultat);
        } catch (Exception e) {
            throw new TekniskException("FP-180125",
                String.format("Tjeneste %s gir feil, meld til #infotrygd_replikering hvis dette skjer gjennom lengre tidsperiode.",
                    restConfig.endpoint()), e);
        }
    }

    @Override
    public List<InfotrygdSak> hentSakerFailSoft(GrunnlagRequest request) {
        try {
            return hentSaker(request);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", restConfig.endpoint(), e);
            return Collections.emptyList();
        }
    }

}
