package no.nav.vedtak.felles.integrasjon.unleash.strategier;

import static java.util.function.Predicate.not;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.finn.unleash.UnleashContext;
import no.finn.unleash.strategy.Strategy;

public class ByAnsvarligSaksbehandlerStrategy implements Strategy {

    static final String SAKSBEHANDLER_IDENT = "SAKSBEHANDLER_IDENT";

    static final String UNLEASH_PROPERTY_NAME_MILJØ = "miljø";
    static final String UNLEASH_PROPERTY_NAME_SAKSBEHANDLER = "saksbehandler";
    static final String UNLEASH_SAKSBEHANDLER_DELIMITER = ",";

    private static final Logger LOGGER = LoggerFactory.getLogger(ByAnsvarligSaksbehandlerStrategy.class);

    @Override
    public String getName() {
        return "byAnsvarligSaksbehandler";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        String saksbehandlerIdent = unleashContext.getProperties().get(SAKSBEHANDLER_IDENT);
        List<String> saksbehandlereLansertFor = getEnabledSaksbehandlere(parameters);
        boolean erLansertForSaksbehandler = saksbehandlerIdent != null && saksbehandlereLansertFor.stream()
                .map(String::trim)
                .anyMatch(saksbehandlerIdent::equalsIgnoreCase);
        boolean erLansertIMiljø = NamespaceUtil.isNamespaceEnabled(parameters, UNLEASH_PROPERTY_NAME_MILJØ);
        boolean erLansert = erLansertForSaksbehandler && erLansertIMiljø;
        return logErEnabled(erLansert, saksbehandlerIdent);
    }

    private boolean logErEnabled(boolean erEnabled, String saksbehandlerIdent) {
        LOGGER.debug("Strategy={} is enabled={} for {}", getName(), erEnabled, saksbehandlerIdent);
        return erEnabled;
    }

    private List<String> getEnabledSaksbehandlere(Map<String, String> parameters) {
        return Optional.ofNullable(parameters)
                .map(param -> param.get(UNLEASH_PROPERTY_NAME_SAKSBEHANDLER))
                .filter(not(String::isEmpty))
                .map(it -> it.split(UNLEASH_SAKSBEHANDLER_DELIMITER))
                .map(Arrays::asList)
                .orElse(List.of());
    }

}
