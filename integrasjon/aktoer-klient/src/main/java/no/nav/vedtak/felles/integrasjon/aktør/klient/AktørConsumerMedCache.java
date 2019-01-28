package no.nav.vedtak.felles.integrasjon.aktør.klient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.AktoerIder;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.IdentDetaljer;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class AktørConsumerMedCache {
    private static final int DEFAULT_CACHE_SIZE = 1000;

    //Satt til 8 timer for å matche cache-lengde brukt i ABAC-løsningen (PDP).
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private AktørConsumer aktørConsumer;
    private LRUCache<String, Optional<String>> cacheAktørIdTilIdent;
    private LRUCache<String, Optional<String>> cacheIdentTilAktørId;

    AktørConsumerMedCache() {
    }

    @Inject
    public AktørConsumerMedCache(AktørConsumer aktørConsumer) {
        this(aktørConsumer, DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public AktørConsumerMedCache(AktørConsumer aktørConsumer, int cacheSize, long cacheTimeoutMillis) {
        this.aktørConsumer = aktørConsumer;
        cacheAktørIdTilIdent = new LRUCache<>(cacheSize, cacheTimeoutMillis);
        cacheIdentTilAktørId = new LRUCache<>(cacheSize, cacheTimeoutMillis);
    }

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
            List<AktoerIder> aktoerIder = aktørConsumer.hentAktørIdForPersonIdentSet(requestSet);
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
            List<AktoerIder> aktoerIder = aktørConsumer.hentAktørIdForPersonIdentSet(requestSet);

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

    public Optional<String> hentAktørIdForPersonIdent(String personIdent) {
        Optional<String> fraCache = cacheIdentTilAktørId.get(personIdent);
        if (fraCache != null) { //NOSONAR trenger null-sjekk selv om bruker optional. Null betyr "finnes ikke i cache". Optional.empty betyr "finnes ikke i TPS"
            return fraCache;
        }
        Optional<String> aktørId = aktørConsumer.hentAktørIdForPersonIdent(personIdent);
        cacheIdentTilAktørId.put(personIdent, aktørId);
        return aktørId;
    }

    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        Optional<String> fraCache = cacheAktørIdTilIdent.get(aktørId);
        if (fraCache != null) { //NOSONAR trenger null-sjekk selv om bruker optional. Null betyr "finnes ikke i cache". Optional.empty betyr "finnes ikke i TPS"
            return fraCache;
        }
        Optional<String> ident = aktørConsumer.hentPersonIdentForAktørId(aktørId);
        cacheAktørIdTilIdent.put(aktørId, ident);
        return ident;
    }
}
