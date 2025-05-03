package no.nav.vedtak.sikkerhet.populasjon;

import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.vedtak.felles.integrasjon.populasjon.AbstractTilgangKlient;
import no.nav.vedtak.felles.integrasjon.populasjon.PopulasjonDto;
import no.nav.vedtak.felles.integrasjon.populasjon.PopulasjonTilgangResultat;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.tilgang.AnsattGruppeKlient;
import no.nav.vedtak.sikkerhet.tilgang.PopulasjonKlient;
import no.nav.vedtak.sikkerhet.tilgang.TilgangResultat;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPTILGANG)
public class TilgangKlient extends AbstractTilgangKlient implements AnsattGruppeKlient, PopulasjonKlient {


    public TilgangKlient() {
        super();
    }

    @Override
    public Set<AnsattGruppe> vurderAnsattGrupper(UUID ansattOid, Set<AnsattGruppe> påkrevdeGrupper) {
        return super.vurderGrupper(ansattOid, påkrevdeGrupper);
    }

    @Override
    public Tilgangsvurdering vurderTilgangInternBruker(UUID ansattOid, Set<String> identer, String saksnummer, UUID behandling) {
        var resultat = super.vurderInternBruker(ansattOid, identer, saksnummer, behandling);
        return mapTilgangsvurdering(resultat);
    }

    @Override
    public Tilgangsvurdering vurderTilgangEksternBruker(String subjectPersonIdent, Set<String> identer, int aldersgrense) {
        var resultat = super.vurderEksternBruker(subjectPersonIdent, identer, aldersgrense);
        return mapTilgangsvurdering(resultat);
    }


    private static Tilgangsvurdering mapTilgangsvurdering(PopulasjonDto.Respons tilgangsvurdering) {
        return new Tilgangsvurdering(map(tilgangsvurdering.tilgangResultat()), tilgangsvurdering.årsak(), Set.of(), tilgangsvurdering.auditIdent());
    }

    private static TilgangResultat map(PopulasjonTilgangResultat tilgangResultat) {
        return switch (tilgangResultat) {
            case GODKJENT -> TilgangResultat.GODKJENT;
            case AVSLÅTT_KODE_7 -> TilgangResultat.AVSLÅTT_KODE_7;
            case AVSLÅTT_KODE_6 -> TilgangResultat.AVSLÅTT_KODE_6;
            case AVSLÅTT_EGEN_ANSATT -> TilgangResultat.AVSLÅTT_EGEN_ANSATT;
            case AVSLÅTT_ANNEN_ÅRSAK -> TilgangResultat.AVSLÅTT_ANNEN_ÅRSAK;
        };
    }

}
