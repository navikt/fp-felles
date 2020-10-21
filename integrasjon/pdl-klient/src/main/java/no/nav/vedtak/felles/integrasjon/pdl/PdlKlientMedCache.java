package no.nav.vedtak.felles.integrasjon.pdl;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
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
    private LRUCache<String, Optional<String>> cacheAktørIdTilIdent;
    private LRUCache<String, Optional<String>> cacheIdentTilAktørId;

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

    PdlKlientMedCache(PdlKlient pdlKlient, LRUCache<String, Optional<String>> cacheAktørIdTilIdent, LRUCache<String, Optional<String>> cacheIdentTilAktørId) {
        this.pdlKlient = pdlKlient;
        this.cacheAktørIdTilIdent = cacheAktørIdTilIdent;
        this.cacheIdentTilAktørId = cacheIdentTilAktørId;
    }

    /*
    public Set<String> hentAktørIdForPersonIdentSet(Set<String> personIdentSet) {
        Set<String> resultSet = new HashSet<>();
        Set<String> requestSet = new HashSet<>();
        for (String personIdent : personIdentSet) {
            Optional<String> fraCache = cacheIdentTilAktørId.get(personIdent);
            if (fraCache != null) { //NOSONAR trenger null-sjekk selv om bruker optional. Null betyr "finnes ikke i cache". Optional.empty betyr "finnes ikke i TPS"
                fraCache.ifPresent(resultSet::add);
            } else {
                requestSet.add(personIdent);
            }
        }
        if (!requestSet.isEmpty()) {
            List<AktoerIder> aktoerIder = pdlKlient.hentAktørIdForPersonIdentSet(requestSet);
            for (AktoerIder aktør : aktoerIder) {
                Optional<String> aktørId = Optional.of(aktør.getAktoerId());
                cacheIdentTilAktørId.put(aktør.getGjeldendeIdent().getTpsId(), aktørId);
                aktør.getHistoriskIdentListe().stream().map(a -> a.getTpsId()).forEach(a -> cacheIdentTilAktørId.put(a, aktørId));
                resultSet.add(aktør.getAktoerId());
            }
        }
        return resultSet;
    }

    public Map<String, String> hentAktørIdMapForPersonIdent(Set<String> personIdentSet) {
        Map<String, String> resultMap = new HashMap<>();
        Set<String> requestSet = new HashSet<>();
        for (String personIdent : personIdentSet) {
            Optional<String> fraCache = cacheIdentTilAktørId.get(personIdent);
            if (fraCache != null) { //NOSONAR trenger null-sjekk selv om bruker optional. Null betyr "finnes ikke i cache". Optional.empty betyr "finnes ikke i TPS"
                fraCache.ifPresent(a -> resultMap.put(personIdent, a));
            } else {
                requestSet.add(personIdent);
            }
        }
        if (!requestSet.isEmpty()) {
            List<AktoerIder> aktoerIder = pdlKlient.hentAktørIdForPersonIdentSet(requestSet);

            for (String ident : requestSet) {

                Optional<AktoerIder> aktør = aktoerIder.stream().filter(a -> a.getGjeldendeIdent().getTpsId().matches(ident)).findFirst();
                if (!aktør.isPresent()) {
                    aktør = aktoerIder.stream().filter(a -> a.getHistoriskIdentListe().stream().anyMatch(i -> i.getTpsId().matches(ident))).findFirst();
                }
                if (aktør.isPresent()) {

                    Optional<String> aktørId = Optional.of(aktør.get().getAktoerId());
                    cacheIdentTilAktørId.put(aktør.get().getGjeldendeIdent().getTpsId(), aktørId);
                    aktør.get().getHistoriskIdentListe().stream().map(IdentDetaljer::getTpsId).forEach(a -> cacheIdentTilAktørId.put(a, aktørId));
                    resultMap.put(ident, aktør.get().getAktoerId());
                }
            }
        }
        return resultMap;
    }
*/

    public Optional<String> hentAktørIdForPersonIdent(String personIdent) {

        Optional<String> fraCache = cacheIdentTilAktørId.get(personIdent);

        if (fraCache != null) { //NOSONAR trenger null-sjekk selv om bruker optional. Null betyr "finnes ikke i cache". Optional.empty betyr "finnes ikke i TPS"
            return fraCache;
        }

        Optional<String> aktørId = getIdenter(IdentGruppe.AKTORID, Tema.OMS, personIdent);

        cacheIdentTilAktørId.put(personIdent, aktørId);

        return aktørId;
    }

    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        Optional<String> fraCache = cacheAktørIdTilIdent.get(aktørId);
        if (fraCache != null) { //NOSONAR trenger null-sjekk selv om bruker optional. Null betyr "finnes ikke i cache". Optional.empty betyr "finnes ikke i TPS"
            return fraCache;
        }
        Optional<String> ident = getIdenter(IdentGruppe.FOLKEREGISTERIDENT, Tema.OMS, aktørId);
        cacheAktørIdTilIdent.put(aktørId, ident);
        return ident;
    }

    public Optional<String> getIdenter(IdentGruppe identGruppe, Tema tema, String aktørId) {

        HentIdenterQueryRequest request = new HentIdenterQueryRequest();
        request.setIdent(aktørId);

        IdentlisteResponseProjection projeksjon = new IdentlisteResponseProjection()
            .identer(
                new IdentInformasjonResponseProjection()
                    .ident()
                    .gruppe()
            );

        Identliste identliste = pdlKlient.hentIdenter(request, projeksjon, tema);

        return identliste.getIdenter().stream().filter(s -> s.getGruppe().equals(identGruppe)).findFirst().map(i -> i.getIdent());
    }

    public Set<String> hentAktørIdForPersonIdentSet(Set<String> personIdentSet) {




        return Collections.emptySet();
    }

    public Map<String, String> hentAktørIdMapForPersonIdent(Set<String> personIdentSet) {
        return Collections.emptyMap();
    }
}




