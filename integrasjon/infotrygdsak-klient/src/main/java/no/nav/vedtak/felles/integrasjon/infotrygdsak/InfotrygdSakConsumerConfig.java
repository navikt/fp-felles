package no.nav.vedtak.felles.integrasjon.infotrygdsak;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.InfotrygdSakV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class InfotrygdSakConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/infotrygdSak/v1/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/infotrygdSak/v1/Binding";

    private String endpointUrl;

    @Inject
    public InfotrygdSakConsumerConfig(@KonfigVerdi("InfotrygdSak_v1.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    InfotrygdSakV1 getPort() {
        return ClientHelper.createServicePort(endpointUrl, InfotrygdSakV1.class, WSDL, NAMESPACE,"InfotrygdSak_v1", "infotrygdSak_v1Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
