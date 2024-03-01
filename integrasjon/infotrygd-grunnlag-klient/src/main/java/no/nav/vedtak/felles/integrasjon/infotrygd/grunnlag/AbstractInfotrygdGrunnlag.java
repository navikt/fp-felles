package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

// Extend og annoter med endpoint, evt default. tokenConfig kan settes til TokenFlow.STS_CC eller AZURE_CC
public abstract class AbstractInfotrygdGrunnlag implements InfotrygdGrunnlag {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractInfotrygdGrunnlag.class);

    private final RestClient restClient;
    private final RestConfig restConfig;

    protected AbstractInfotrygdGrunnlag() {
        this(RestClient.client());
    }

    protected AbstractInfotrygdGrunnlag(RestClient client) {
        this.restClient = client;
        this.restConfig = RestConfig.forClient(this.getClass());
    }

    @Override
    public List<Grunnlag> hentGrunnlag(GrunnlagRequest request) {
        try {
            var rrequest = RestRequest.newPOSTJson(request, restConfig.endpoint(), restConfig);
            var resultat = restClient.send(rrequest, Grunnlag[].class);
            return Arrays.asList(resultat);
        } catch (Exception e) {
            throw new TekniskException("FP-180125",
                String.format("Tjeneste %s gir feil, meld til #infotrygd_replikering hvis dette skjer gjennom lengre tidsperiode.",
                    restConfig.endpoint()), e);
        }
    }

    @Override
    public List<Grunnlag> hentGrunnlagFailSoft(GrunnlagRequest request) {
        try {
            return hentGrunnlag(request);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", restConfig.endpoint(), e);
            return Collections.emptyList();
        }
    }

}
