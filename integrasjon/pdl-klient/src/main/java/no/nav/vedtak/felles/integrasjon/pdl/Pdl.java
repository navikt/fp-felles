package no.nav.vedtak.felles.integrasjon.pdl;

import static no.nav.pdl.IdentGruppe.AKTORID;
import static no.nav.pdl.IdentGruppe.FOLKEREGISTERIDENT;

import java.util.List;
import java.util.Optional;

import no.nav.pdl.GeografiskTilknytning;
import no.nav.pdl.GeografiskTilknytningResponseProjection;
import no.nav.pdl.HentGeografiskTilknytningQueryRequest;
import no.nav.pdl.HentIdenterBolkQueryRequest;
import no.nav.pdl.HentIdenterBolkResult;
import no.nav.pdl.HentIdenterBolkResultResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLQueryable;

public interface Pdl extends GraphQLQueryable {

    static final String PDL_ERROR_RESPONSE = "F-399735";
    static final String PDL_IO_EXCEPTION = "F-539237";
    static final String PDL_INTERNAL = "F-539238";
    public static final String PDL_KLIENT_NOT_FOUND_KODE = "F-399736";

    default Optional<String> hentPersonIdentForAktørId(String aktørId) {
        return query(aktørId, FOLKEREGISTERIDENT);
    }

    default Optional<String> hentAktørIdForPersonIdent(String personIdent) {
        return query(personIdent, AKTORID);
    }

    List<HentIdenterBolkResult> hentIdenterBolkResults(HentIdenterBolkQueryRequest q, HentIdenterBolkResultResponseProjection p);

    Identliste hentIdenter(HentIdenterQueryRequest q, IdentlisteResponseProjection p);

    Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p);

    GeografiskTilknytning hentGT(HentGeografiskTilknytningQueryRequest q, GeografiskTilknytningResponseProjection p);

    private Optional<String> query(String aktørId, IdentGruppe gruppe) {
        var query = new HentIdenterQueryRequest();
        query.setIdent(aktørId);
        return hentIdenter(query, new IdentlisteResponseProjection()
                .identer(new IdentInformasjonResponseProjection()
                        .ident()
                        .gruppe())).getIdenter()
                                .stream()
                                .filter(s -> s.getGruppe().equals(gruppe))
                                .findFirst()
                                .map(IdentInformasjon::getIdent);
    }
}
