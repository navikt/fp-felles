package no.nav.vedtak.sikkerhet.oidc.validator;

import static no.nav.vedtak.log.metrics.MetricsUtil.REGISTRY;

import io.micrometer.core.instrument.Counter;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public class ConsumerMetric {

    private static final String FORELDREPENGER_KONSUMENTER = "foreldrepenger.konsumenter";

    private ConsumerMetric() {
    }

    public static void registrer(String klientNavn, String konsument, OpenIDProvider tokenType, IdentType identType, String acrLevel) {
        counter(klientNavn, konsument, tokenType, identType, acrLevel).increment();
    }
    public static void registrer(String klientNavn, String konsument, OpenIDProvider tokenType, IdentType identType) {
        counter(klientNavn, konsument, tokenType, identType, null).increment();
    }

    private static Counter counter(String klientNavn, String konsument, OpenIDProvider tokenType, IdentType identType, String acrLevel) {
        var counter = Counter.builder(FORELDREPENGER_KONSUMENTER)
            .tag("klient", klientNavn)
            .tag("tokenType", tokenType.name())
            .tag("identYype", identType.name())
            .tag("konsument", konsument)
            .description("Konsument og token brukt.");

        if (acrLevel != null) {
            counter.tag("acrLevel", acrLevel);
        }
        return counter.register(REGISTRY);
    }
}
