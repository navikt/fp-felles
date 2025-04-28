package no.nav.vedtak.sikkerhet.populasjon;

import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.tilgang.AnsattGruppeKlient;
import no.nav.vedtak.sikkerhet.tilgang.PopulasjonKlient;
import no.nav.vedtak.sikkerhet.tilgang.TilgangResultat;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPTILGANG)
public class TilgangKlient implements AnsattGruppeKlient, PopulasjonKlient {

    private final URI ansattGruppeUri;
    private final URI internBrukerUri;
    private final URI eksternBrukerUri;
    private final RestClient klient;
    private final RestConfig restConfig;

    public TilgangKlient() {
        this.klient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.ansattGruppeUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/ansatt/utvidet/gruppemedlemskap-uid")
            .build();
        this.internBrukerUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/populasjon/internbruker")
            .build();
        this.eksternBrukerUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/populasjon/eksternbruker")
            .build();
    }

    @Override
    public Set<AnsattGruppe> vurderAnsattGrupper(UUID ansattOid, Set<AnsattGruppe> påkrevdeGrupper) {
        var request = new UidGruppeDto(ansattOid, påkrevdeGrupper);
        var rrequest = RestRequest.newPOSTJson(request, ansattGruppeUri, restConfig).timeout(Duration.ofSeconds(3));
        return klient.sendReturnOptional(rrequest, GruppeDto.class).map(GruppeDto::grupper).orElseGet(Set::of);
    }

    @Override
    public Tilgangsvurdering vurderTilgangInternBruker(UUID ansattOid, Set<String> identer, String saksnummer, UUID behandling) {
        var request = new PopulasjonInternRequest(ansattOid, identer, saksnummer, behandling);
        var rrequest = RestRequest.newPOSTJson(request, internBrukerUri, restConfig).timeout(Duration.ofSeconds(3));
        var resultat = klient.send(rrequest, TilgangsvurderingRespons.class);
        return mapTilgangsvurdering(resultat);
    }

    @Override
    public Tilgangsvurdering vurderTilgangEksternBruker(String subjectPersonIdent, Set<String> identer, int aldersgrense) {
        var request = new PopulasjonEksternRequest(subjectPersonIdent, identer, aldersgrense);
        var rrequest = RestRequest.newPOSTJson(request, eksternBrukerUri, restConfig).timeout(Duration.ofSeconds(3));
        var resultat = klient.send(rrequest, TilgangsvurderingRespons.class);
        return mapTilgangsvurdering(resultat);
    }

    private record GruppeDto(@NotNull @Valid Set<AnsattGruppe> grupper) { }


    private record UidGruppeDto(@NotNull UUID uid, @Valid @NotNull Set<AnsattGruppe> grupper) { }

    public record PopulasjonInternRequest(@NotNull UUID ansattOid,
                                          @NotNull @Valid Set<String> identer,
                                          @Valid String saksnummer,
                                          @Valid UUID behandling) { }

    public record PopulasjonEksternRequest(@NotNull String subjectPersonIdent,
                                           @NotNull @Valid Set<String> identer,
                                           @Valid int aldersgrense) { }

    public record TilgangsvurderingRespons(@NotNull TilgangResultat tilgangResultat,
                                           String årsak,
                                           String auditIdent) { }

    private Tilgangsvurdering mapTilgangsvurdering(TilgangsvurderingRespons tilgangsvurdering) {
        return new Tilgangsvurdering(tilgangsvurdering.tilgangResultat(), tilgangsvurdering.årsak(), Set.of(), tilgangsvurdering.auditIdent());
    }

}
