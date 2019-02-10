package no.nav.vedtak.felles.integrasjon.dokument.produksjon;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.binding.DokumentproduksjonV2;
import no.nav.vedtak.felles.integrasjon.felles.ws.ClientHelper;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class DokumentproduksjonConsumerConfig {
    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/dokumentproduksjon/v2/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/dokumentproduksjon/v2/Binding";

    @Inject
    @KonfigVerdi("Dokumentproduksjon_v2.url")
    private String endpointUrl;  // NOSONAR

    DokumentproduksjonV2 getPort() {
        return ClientHelper.createServicePort(endpointUrl, DokumentproduksjonV2.class, WSDL, NAMESPACE,"Dokumentproduksjon_v2", "Dokumentproduksjon_v2Port");
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
