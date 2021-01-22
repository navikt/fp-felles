package no.nav.vedtak.felles.integrasjon.pdl;

import java.util.List;

import no.nav.pdl.GeografiskTilknytning;
import no.nav.pdl.GeografiskTilknytningResponseProjection;
import no.nav.pdl.HentGeografiskTilknytningQueryRequest;
import no.nav.pdl.HentIdenterBolkQueryRequest;
import no.nav.pdl.HentIdenterBolkResult;
import no.nav.pdl.HentIdenterBolkResultResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;

/**
 *
 * @deprecated, bruk {@link PDLQueryable} direkte
 *
 */
@Deprecated(since = "3.0.54")
public interface Pdl extends PDLQueryable {

    List<HentIdenterBolkResult> hentIdenterBolkResults(HentIdenterBolkQueryRequest q, HentIdenterBolkResultResponseProjection p);

    Identliste hentIdenter(HentIdenterQueryRequest q, IdentlisteResponseProjection p);

    Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p);

    GeografiskTilknytning hentGT(HentGeografiskTilknytningQueryRequest q, GeografiskTilknytningResponseProjection p);

}
