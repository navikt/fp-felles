package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

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
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom) {
        try {
            var path = UriBuilder.fromUri(restConfig.endpoint())
                .queryParam("fnr", fnr)
                .queryParam("fom", konverter(fom))
                .queryParam("tom", konverter(tom))
                .build();
            return restClient.sendReturnList(RestRequest.newGET(path, restConfig), Grunnlag.class);
        } catch (Exception e) {
            throw new TekniskException("FP-180125",
                String.format("Tjeneste %s gir feil, meld til #infotrygd_replikering hvis dette skjer gjennom lengre tidsperiode.",
                    restConfig.endpoint()), e);
        }
    }

    @Override
    public List<Grunnlag> hentGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom) {
        try {
            return hentGrunnlag(fnr, fom, tom);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", restConfig.endpoint(), e);
            return Collections.emptyList();
        }
    }

    private static String konverter(LocalDate dato) {
        var brukDato = Optional.ofNullable(dato).orElseGet(LocalDate::now);
        return brukDato.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    protected RestClient getRestClient() {
        return restClient;
    }

    protected RestConfig getRestConfig() {
        return restConfig;
    }

}
