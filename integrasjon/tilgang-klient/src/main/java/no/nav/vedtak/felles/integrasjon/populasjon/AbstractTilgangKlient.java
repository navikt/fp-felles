package no.nav.vedtak.felles.integrasjon.populasjon;

import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import jakarta.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.ansatt.GrupperDto;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;


public abstract class AbstractTilgangKlient {

    private final URI ansattGruppeUri;
    private final URI internBrukerUri;
    private final URI eksternBrukerUri;
    private final RestClient klient;
    private final RestConfig restConfig;

    protected AbstractTilgangKlient() {
        this.klient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.ansattGruppeUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/ansattinfo/grupper-filter")
            .build();
        this.internBrukerUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/populasjon/internbruker")
            .build();
        this.eksternBrukerUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/populasjon/eksternbruker")
            .build();
    }

    protected Set<AnsattGruppe> vurderGrupper(UUID ansattOid, Set<AnsattGruppe> påkrevdeGrupper) {
        var request = new GrupperDto.FilterRequest(ansattOid, påkrevdeGrupper);
        var rrequest = RestRequest.newPOSTJson(request, ansattGruppeUri, restConfig).timeout(Duration.ofSeconds(3));
        return klient.sendReturnOptional(rrequest, GrupperDto.Respons.class).map(GrupperDto.Respons::grupper).orElseGet(Set::of);
    }

    protected PopulasjonDto.Respons vurderInternBruker(UUID ansattOid, Set<String> identer, String saksnummer, UUID behandling) {
        var request = new PopulasjonDto.InternRequest(ansattOid, identer, saksnummer, behandling);
        var rrequest = RestRequest.newPOSTJson(request, internBrukerUri, restConfig).timeout(Duration.ofSeconds(3));
        return klient.send(rrequest, PopulasjonDto.Respons.class);
    }

    protected PopulasjonDto.Respons vurderEksternBruker(String subjectPersonIdent, Set<String> identer, int aldersgrense) {
        var request = new PopulasjonDto.EksternRequest(subjectPersonIdent, identer, aldersgrense);
        var rrequest = RestRequest.newPOSTJson(request, eksternBrukerUri, restConfig).timeout(Duration.ofSeconds(3));
        return klient.send(rrequest, PopulasjonDto.Respons.class);
    }

}
