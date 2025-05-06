package no.nav.vedtak.felles.integrasjon.ansatt;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;

public abstract class AbstractAnsattInfoKlient {

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI grupperMedlemUri;
    private final URI grupperFilterUri;
    private final URI ansattIdentUri;
    private final URI ansattOidUri;


    public AbstractAnsattInfoKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.ansattIdentUri = UriBuilder.fromUri(restConfig.fpContextPath()).path("/api/ansattinfo/ansatt-ident").build();
        this.ansattOidUri = UriBuilder.fromUri(restConfig.fpContextPath()).path("/api/ansattinfo/ansatt-oid").build();
        this.grupperMedlemUri = UriBuilder.fromUri(restConfig.fpContextPath()).path("/api/ansattinfo/grupper-medlem").build();
        this.grupperFilterUri = UriBuilder.fromUri(restConfig.fpContextPath()).path("/api/ansattinfo/grupper-filter").build();
    }

    protected AnsattInfoDto.Respons hentAnsattInfoForIdent(String ident) {
        var request = RestRequest.newPOSTJson(new AnsattInfoDto.IdentRequest(ident), ansattIdentUri, restConfig);
        return restClient.send(request, AnsattInfoDto.Respons.class);
    }

    protected AnsattInfoDto.Respons hentAnsattInfoForOid(UUID saksbehandler) {
        var request = RestRequest.newPOSTJson(new AnsattInfoDto.OidRequest(saksbehandler), ansattOidUri, restConfig);
        return restClient.send(request, AnsattInfoDto.Respons.class);
    }

    protected Set<AnsattGruppe> alleGrupper(UUID saksbehandler) {
        var request = RestRequest.newPOSTJson(new GrupperDto.MedlemRequest(saksbehandler), grupperMedlemUri, restConfig);
        return restClient.sendReturnOptional(request, GrupperDto.Respons.class).map(GrupperDto.Respons::grupper).orElseGet(Set::of);
    }

    protected Set<AnsattGruppe> filtrerGrupper(UUID saksbehandler, List<AnsattGruppe> grupper) {
        var request = RestRequest.newPOSTJson(new GrupperDto.FilterRequest(saksbehandler, new HashSet<>(grupper)), grupperFilterUri, restConfig);
        return restClient.sendReturnOptional(request, GrupperDto.Respons.class).map(GrupperDto.Respons::grupper).orElseGet(Set::of);
    }

}
