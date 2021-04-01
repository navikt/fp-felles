package no.nav.vedtak.felles.integrasjon.unleash;

import javax.enterprise.inject.Produces;

import no.finn.unleash.Unleash;

public class UnleashProvider {
    @Produces
    public Unleash unleashProducer() {
        return new ToggleConfig().unleash();
    }
}
