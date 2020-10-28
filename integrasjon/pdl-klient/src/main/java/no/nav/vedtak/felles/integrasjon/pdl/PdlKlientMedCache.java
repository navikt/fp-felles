package no.nav.vedtak.felles.integrasjon.pdl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class PdlKlientMedCache {
    private static final int DEFAULT_CACHE_SIZE = 1000;

    //Satt til 8 timer for å matche cache-lengde brukt i ABAC-løsningen (PDP).
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private PdlKlient pdlKlient;
    private LRUCache<String, String> cacheAktørIdTilIdent;
    private LRUCache<String, String> cacheIdentTilAktørId;

    PdlKlientMedCache() {
    }

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

        Optional<String> aktørIdFraCache = Optional.ofNullable(cacheIdentTilAktørId.get(personIdent));

        if (aktørIdFraCache.isPresent()) {
            return aktørIdFraCache;
        }

        Optional<String> aktørId = getIdente(IdentGruppe.AKTORID, tema, personIdent);

        aktørId.ifPresent(s -> cacheIdentTilAktørId.put(personIdent, s));

        return aktørId;
    }

    public Optional<String> hentPersonIdentForAktørId(String aktørId, Tema tema) {
        Optional<String> personIdentFraCache = Optional.ofNullable(cacheAktørIdTilIdent.get(aktørId));

        if (personIdentFraCache.isPresent()) {
            return personIdentFraCache;
        }

        Optional<String> personident = getIdente(IdentGruppe.FOLKEREGISTERIDENT, tema, aktørId);

        personident.ifPresent(i -> cacheAktørIdTilIdent.put(aktørId, i));

        return personident;
    }

    public Optional<String> getIdente(IdentGruppe identGruppe, Tema tema, String aktørId) {

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

    public Set<String> hentAktørIdForPersonIdentSet(Set<String> personIdentSet) {
        /**
         * sjekk om person finnes i cache og ta vare på aktøridentene
         *
         * for alle som ikke finnes i cache, samle opp disse personide'ne
         *
         * kjør ny bolk-spørring mot pdlklient, og hent ut resterende aktørid'er
         *
         * Lag set av aktørid'er og returner det
         */
        Set<String> resultset = new HashSet<>();
        Set<String> requestset = new HashSet<>();

/*
        List<String> finnesICache = personIdentSet.stream()
            .filter(ident -> cacheIdentTilAktørId.get(ident) != null)
            .map(ident -> cacheIdentTilAktørId.get(ident))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
*/


        // kjør ny bolk-spørring mot pdlklient


        return Collections.emptySet();
    }

    public Map<String, String> hentAktørIdMapForPersonIdent(Set<String> personIdentSet) {
        return Collections.emptyMap();
    }
}




