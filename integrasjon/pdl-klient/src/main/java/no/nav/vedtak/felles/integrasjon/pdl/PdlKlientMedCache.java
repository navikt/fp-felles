package no.nav.vedtak.felles.integrasjon.pdl;

import static java.util.List.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.pdl.HentIdenterBolkQueryRequest;
import no.nav.pdl.HentIdenterBolkResultResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.util.LRUCache;
import no.nav.vedtak.util.Tuple;

@ApplicationScoped
public class PdlKlientMedCache {
    private static final int DEFAULT_CACHE_SIZE = 1000;

    //Satt til 8 timer for å matche cache-lengde brukt i ABAC-løsningen (PDP).
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private final PdlKlient pdlKlient;
    private final LRUCache<String, String> cacheAktørIdTilIdent;
    private final LRUCache<String, String> cacheIdentTilAktørId;

    @Inject
    public PdlKlientMedCache(PdlKlient pdlKlient) {
        this(pdlKlient, DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public PdlKlientMedCache(PdlKlient pdlKlient, int cacheSize, long cacheTimeoutMillis) {
        this.pdlKlient = pdlKlient;
        cacheAktørIdTilIdent = new LRUCache<>(cacheSize, cacheTimeoutMillis);
        cacheIdentTilAktørId = new LRUCache<>(cacheSize, cacheTimeoutMillis);
    }

    PdlKlientMedCache(PdlKlient pdlKlient, LRUCache<String, String> cacheAktørIdTilIdent, LRUCache<String, String> cacheIdentTilAktørId) {
        this.pdlKlient = pdlKlient;
        this.cacheAktørIdTilIdent = cacheAktørIdTilIdent;
        this.cacheIdentTilAktørId = cacheIdentTilAktørId;
    }

    public Optional<String> hentAktørIdForPersonIdent(String personIdent, Tema tema) {

        Optional<String> aktørIdFraCache = ofNullable(cacheIdentTilAktørId.get(personIdent));

        if (aktørIdFraCache.isPresent()) {
            return aktørIdFraCache;
        }

        Optional<String> aktørId = identFor(IdentGruppe.AKTORID, tema, personIdent);

        aktørId.ifPresent(s -> cacheIdentTilAktørId.put(personIdent, s));

        return aktørId;
    }

    public Optional<String> hentPersonIdentForAktørId(String aktørId, Tema tema) {
        Optional<String> personIdentFraCache = ofNullable(cacheAktørIdTilIdent.get(aktørId));

        if (personIdentFraCache.isPresent()) {
            return personIdentFraCache;
        }

        Optional<String> personident = identFor(IdentGruppe.FOLKEREGISTERIDENT, tema, aktørId);

        personident.ifPresent(i -> cacheAktørIdTilIdent.put(aktørId, i));

        return personident;
    }

    public Set<String> hentAktørIdForPersonIdentSet(Set<String> personIdentSet, Tema tema) {
        var personIdentIkkeICache =
            personIdentSet.stream()
                .filter(ident -> ofNullable(cacheIdentTilAktørId.get(ident)).isEmpty())
                .collect(toList());

        return concat(
            personIdentSet.stream()
                .map(ident -> ofNullable(cacheIdentTilAktørId.get(ident)))
                .flatMap(Optional::stream),
            hentBolkMedAktørId(tema, personIdentIkkeICache)
                .peek(aktørInfo -> cacheIdentTilAktørId.put(aktørInfo.getElement1(), aktørInfo.getElement2().getIdent()))
                .map(aktørinfo -> aktørinfo.getElement2().getIdent())
        )
            .collect(Collectors.toSet());
    }

    private Stream<Tuple<String, IdentInformasjon>> hentBolkMedAktørId(Tema tema, List<String> personIdents) {
        HentIdenterBolkQueryRequest query = new HentIdenterBolkQueryRequest();
        query.setIdenter(personIdents);
        query.setGrupper(of(IdentGruppe.AKTORID));

        var projection = new HentIdenterBolkResultResponseProjection()
            .ident()
            .identer(new IdentInformasjonResponseProjection()
                .ident()
                .gruppe()
            )
            .code();

        Predicate<IdentInformasjon> erØnsketIdentgruppe = identInformasjon -> identInformasjon.getGruppe().equals(IdentGruppe.AKTORID);

        //noinspection OptionalGetWithoutIsPresent
        return pdlKlient.hentIdenterBolkResults(query, projection, tema).stream()
            .filter(r -> r.getIdenter().stream().anyMatch(erØnsketIdentgruppe))
            .map(r -> new Tuple<>(r.getIdent(), r.getIdenter().stream().filter(erØnsketIdentgruppe).findAny().get()));
    }

    private Optional<String> identFor(IdentGruppe identGruppe, Tema tema, String aktørId) {
        HentIdenterQueryRequest request = new HentIdenterQueryRequest();
        request.setIdent(aktørId);

        IdentlisteResponseProjection projeksjon = new IdentlisteResponseProjection()
            .identer(
                new IdentInformasjonResponseProjection()
                    .ident()
                    .gruppe()
            );

        Identliste identliste = pdlKlient.hentIdenter(request, projeksjon, tema);

        return identliste.getIdenter().stream().filter(s -> s.getGruppe().equals(identGruppe)).findFirst().map(IdentInformasjon::getIdent);
    }

    //TODO Trenger vi denne, er ekvivalenten i bruk noe sted Jens-Otto?
    public Map<String, String> hentAktørIdMapForPersonIdent(Set<String> personIdentSet) {
        return Collections.emptyMap();
    }
}




