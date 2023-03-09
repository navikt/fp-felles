package no.nav.vedtak.felles.integrasjon.person;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;
import no.nav.pdl.*;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Optional;

import static no.nav.pdl.IdentGruppe.AKTORID;
import static no.nav.pdl.IdentGruppe.FOLKEREGISTERIDENT;

/*
 * PDL kan kalles i 3 ganske ulike sammenhenger: Systemressurs, EksternBruker, InternBruker,
 */
public interface Persondata {

    String PDL_ERROR_RESPONSE = "F-399735";
    String PDL_IO_EXCEPTION = "F-539237";
    String PDL_INTERNAL = "F-539238";
    String PDL_KLIENT_NOT_FOUND_KODE = "F-399736";

    default Optional<String> hentPersonIdentForAktørId(String aktørId) {
        return hentId(aktørId, FOLKEREGISTERIDENT, false);
    }

    default Optional<String> hentPersonIdentForAktørId(String aktørId, boolean ignoreNotFound) {
        return hentId(aktørId, FOLKEREGISTERIDENT, ignoreNotFound);
    }

    default Optional<String> hentAktørIdForPersonIdent(String personIdent) {
        return hentId(personIdent, AKTORID, false);
    }

    default Optional<String> hentAktørIdForPersonIdent(String personIdent, boolean ignoreNotFound) {
        return hentId(personIdent, AKTORID, ignoreNotFound);
    }

    List<HentIdenterBolkResult> hentIdenterBolkResults(HentIdenterBolkQueryRequest q, HentIdenterBolkResultResponseProjection p);

    Identliste hentIdenter(HentIdenterQueryRequest q, IdentlisteResponseProjection p);

    Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p);

    Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p, boolean ignoreNotFound);

    GeografiskTilknytning hentGT(HentGeografiskTilknytningQueryRequest q, GeografiskTilknytningResponseProjection p);

    <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz);

    private Optional<String> hentId(String id, IdentGruppe gruppe, boolean ignoreNotFound) {
        var query = new HentIdenterQueryRequest();
        query.setIdent(id);
        try {
            return hentIdenter(query, new IdentlisteResponseProjection()
                .identer(new IdentInformasjonResponseProjection()
                    .ident()
                    .gruppe())).getIdenter()
                .stream()
                .filter(s -> s.getGruppe().equals(gruppe))
                .findFirst()
                .map(IdentInformasjon::getIdent);
        } catch (PdlException e) {
            if (e.getStatus() == HttpURLConnection.HTTP_NOT_FOUND && ignoreNotFound) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
