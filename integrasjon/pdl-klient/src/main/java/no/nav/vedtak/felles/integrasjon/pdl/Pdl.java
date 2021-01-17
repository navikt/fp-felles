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

public interface Pdl {
    static final String PDL_ERROR_RESPONSE = "F-399735";
    static final String PDL_IO_EXCEPTION = "F-539237";
    static final String PDL_INTERNAL = "F-539238";
    public static final String PDL_KLIENT_NOT_FOUND_KODE = "F-399736";

    List<HentIdenterBolkResult> hentIdenterBolkResults(HentIdenterBolkQueryRequest q, HentIdenterBolkResultResponseProjection p);

    Identliste hentIdenter(HentIdenterQueryRequest q, IdentlisteResponseProjection p);

    Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p);

    GeografiskTilknytning hentGT(HentGeografiskTilknytningQueryRequest q, GeografiskTilknytningResponseProjection p);

}
