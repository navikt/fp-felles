package no.nav.vedtak.felles.integrasjon.pdl;

import static java.util.List.of;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;
import static no.nav.pdl.IdentGruppe.AKTORID;
import static no.nav.pdl.IdentGruppe.FOLKEREGISTERIDENT;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import no.nav.pdl.HentIdenterBolkQueryRequest;
import no.nav.pdl.HentIdenterBolkResultResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.util.Tuple;

@ApplicationScoped
public class PdlKlientMedCache {
    private static final Logger LOG = LoggerFactory.getLogger(PdlKlientMedCache.class);
    private static final int DEFAULT_CACHE_SIZE = 1000;

    // Satt til 8 timer for å matche cache-lengde brukt i ABAC-løsningen (PDP).
    private static final long DEFAULT_CACHE_TIMEOUT = MILLISECONDS.convert(8, HOURS);

    private final PdlKlient pdlKlient;
    private final Cache<String, String> cacheAktørIdTilIdent;
    private final Cache<String, String> cacheIdentTilAktørId;

    @Inject
    public PdlKlientMedCache(PdlKlient pdlKlient) {
        this(pdlKlient, DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public PdlKlientMedCache(PdlKlient pdlKlient, int cacheSize, long cacheTimeoutMillis) {
        this(pdlKlient, cacheSize, cacheTimeoutMillis, MILLISECONDS);
    }

    public PdlKlientMedCache(PdlKlient pdlKlient, int cacheSize, long timeout, TimeUnit unit) {
        this(pdlKlient, cache(cacheSize, timeout, unit), cache(cacheSize, timeout, unit));
    }

    PdlKlientMedCache(PdlKlient pdlKlient, Cache<String, String> cacheAktørIdTilIdent, Cache<String, String> cacheIdentTilAktørId) {
        this.pdlKlient = pdlKlient;
        this.cacheAktørIdTilIdent = cacheAktørIdTilIdent;
        this.cacheIdentTilAktørId = cacheIdentTilAktørId;
    }

    public Optional<String> hentAktørIdForPersonIdent(String personIdent, Tema tema) {
        return Optional.of(cacheIdentTilAktørId
                .get(personIdent, load(tema, AKTORID)));
    }

    public Optional<String> hentPersonIdentForAktørId(String aktørId, Tema tema) {
        return Optional.of(cacheAktørIdTilIdent
                .get(aktørId, load(tema, FOLKEREGISTERIDENT)));
    }

    public Set<String> hentAktørIdForPersonIdentSet(List<String> ids, Tema tema) {
        return hentBolkMedAktørId(ids, tema)
                .map(Tuple::getElement2)
                .map(IdentInformasjon::getIdent)
                .collect(toSet());
    }

    private Stream<Tuple<String, IdentInformasjon>> hentBolkMedAktørId(List<String> ids, Tema tema) {
        var query = new HentIdenterBolkQueryRequest();
        query.setIdenter(ids);
        query.setGrupper(of(AKTORID));

        var projection = new HentIdenterBolkResultResponseProjection()
                .ident()
                .identer(new IdentInformasjonResponseProjection()
                        .ident()
                        .gruppe())
                .code();

        return pdlKlient.hentIdenterBolkResults(query, projection, tema)
                .stream()
                .filter(r -> r.getIdenter()
                        .stream().anyMatch(gruppe(AKTORID)))
                .map(r -> new Tuple<>(r.getIdent(), r.getIdenter()
                        .stream()
                        .filter(gruppe(AKTORID))
                        .findAny()
                        .get()));

    }

    private Optional<String> identFor(IdentGruppe identGruppe, Tema tema, String aktørId) {
        var request = new HentIdenterQueryRequest();
        request.setIdent(aktørId);
        var projeksjon = new IdentlisteResponseProjection()
                .identer(new IdentInformasjonResponseProjection()
                        .ident()
                        .gruppe());

        return pdlKlient.hentIdenter(request, projeksjon, tema).getIdenter()
                .stream()
                .filter(gruppe(identGruppe))
                .findFirst()
                .map(IdentInformasjon::getIdent);
    }

    private Function<? super String, ? extends String> load(Tema tema, IdentGruppe g) {
        return id -> identFor(g, tema, id)
                .orElseGet(() -> null);
    }

    private static Predicate<? super IdentInformasjon> gruppe(IdentGruppe g) {
        return s -> s.getGruppe().equals(g);
    }

    private static Cache<String, String> cache(int size, long timeout, TimeUnit unit) {
        return Caffeine.newBuilder()
                .expireAfterWrite(timeout, unit)
                .maximumSize(size)
                .removalListener(new RemovalListener<String, String>() {

                    @Override
                    public void onRemoval(String key, String value, RemovalCause cause) {
                        LOG.info("Fjerner {} for {} grunnet {}", value, key, cause);
                    }
                })
                .build();
    }

}
