package no.nav.vedtak.felles.integrasjon.unleash;

import javax.enterprise.inject.Produces;

import no.finn.unleash.Unleash;

@Deprecated(since = "3.2", forRemoval = true)
public class UnleashProvider {
    @Produces
    public Unleash unleashProducer() {
        return new ToggleConfig().unleash();
    }
}
