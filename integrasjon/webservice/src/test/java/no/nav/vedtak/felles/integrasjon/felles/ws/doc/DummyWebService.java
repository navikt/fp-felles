package no.nav.vedtak.felles.integrasjon.felles.ws.doc;

import javax.jws.WebService;

import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebService;

@WebService(wsdlLocation = "wsdl/no/nav/tjeneste/virksomhet/behandleForeldrepengesak/v1/behandleForeldrepengesak.wsdl", serviceName = "BehandleForeldrepengesak_v1", portName = "BehandleForeldrepengesak_v1Port", endpointInterface = "no.nav.tjeneste.virksomhet.behandleforeldrepengesak.v1.binding.BehandleForeldrepengesakV1")
@SoapWebService(endpoint = "/sak/opprettSak/v1", tjenesteBeskrivelseURL = "https://confluence.adeo.no/pages/viewpage.action?pageId=220529015")
public class DummyWebService {

}
