package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.weld.environment.se.Weld;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.tilgang.TilgangResultat;

class BeskyttetRessursInterceptorCdiTest {

    @Test
    void servicePathResolvesAnnotatedBeanClassBehindWeldSubclass() {
        var weld = new Weld().disableDiscovery()
            .addBeanClasses(Ressurs.class, RecordingPep.class, StubTokenProvider.class, BeskyttetRessursInterceptor.class);
        try (var container = weld.initialize()) {
            var ressurs = container.select(Ressurs.class).get();
            var pep = container.select(RecordingPep.class).get();

            assertThat(ressurs.hent()).isEqualTo("ok");
            assertThat(ressurs.getClass()).isNotEqualTo(Ressurs.class);
            assertThat(pep.vurdert().getServicePath()).isEqualTo("/ressurs/hent");
        }
    }

    @ApplicationScoped
    @Path("/ressurs")
    public static class Ressurs {
        @GET
        @Path("/hent")
        @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
        public String hent() {
            return "ok";
        }
    }

    @ApplicationScoped
    public static class RecordingPep implements Pep {
        private final AtomicReference<BeskyttetRessursAttributter> vurdert = new AtomicReference<>();

        BeskyttetRessursAttributter vurdert() {
            return vurdert.get();
        }

        @Override
        public AppRessursData hentRessurser(BeskyttetRessursAttributter attributter) {
            return null;
        }

        @Override
        public TilgangResultat vurderTilgang(BeskyttetRessursAttributter attributter, AppRessursData appRessurser) {
            vurdert.set(attributter);
            return TilgangResultat.GODKJENT;
        }
    }

    @ApplicationScoped
    public static class StubTokenProvider implements TokenProvider {
        @Override
        public String getUid() {
            return "A000000";
        }

        @Override
        public IdentType getIdentType() {
            return IdentType.InternBruker;
        }

        @Override
        public OpenIDToken openIdToken() {
            return null;
        }

        @Override
        public UUID getOid() {
            return UUID.randomUUID();
        }

        @Override
        public Set<AnsattGruppe> getAnsattGrupper() {
            return Set.of();
        }
    }
}
