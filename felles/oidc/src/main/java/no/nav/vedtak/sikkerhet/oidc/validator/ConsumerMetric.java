package no.nav.vedtak.sikkerhet.oidc.validator;

import static no.nav.vedtak.log.metrics.MetricsUtil.REGISTRY;

import io.micrometer.core.instrument.Counter;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public class ConsumerMetric {

    private static final Environment ENV = Environment.current();
    private static final String FORELDREPENGER_KONSUMENTER = "foreldrepenger.konsumenter";

    private ConsumerMetric() {
    }

    public static void registrer(String konsument, OpenIDProvider tokenType, IdentType identType, String acrLevel) {
        counter(konsument, tokenType, identType, acrLevel).increment();
    }
    public static void registrer(String konsument, OpenIDProvider tokenType, IdentType identType) {
            counter(konsument, tokenType, identType, "").increment();
    }

    private static Counter counter(String konsument, OpenIDProvider tokenType, IdentType identType, String acrLevel) {
        return Counter.builder(FORELDREPENGER_KONSUMENTER)
            .tag("klient", ENV.getClientId().getClientId())
            .tag("tokenType", tokenType.name())
            .tag("identYype", identType.name())
            .tag("konsument", konsument)
            .tag("acrLevel", acrLevel)
            .description("Konsument og token brukt.")
            .register(REGISTRY);
    }
}
