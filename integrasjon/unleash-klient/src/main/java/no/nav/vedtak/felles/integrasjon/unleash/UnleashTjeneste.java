package no.nav.vedtak.felles.integrasjon.unleash;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;

@ApplicationScoped
public class UnleashTjeneste implements Unleash {

    private Unleash unleash;

    public UnleashTjeneste() {
    }

    @Override
    public boolean isEnabled(String toggle) {
        return getUnleash().isEnabled(toggle);
    }

    @Override
    public boolean isEnabled(String toggleName, UnleashContext context) {
        return this.isEnabled(toggleName, context, false);
    }

    @Override
    public boolean isEnabled(String toggleName, UnleashContext context, boolean defaultSetting) {
        return getUnleash().isEnabled(toggleName, context, defaultSetting);
    }

    @Override
    public boolean isEnabled(String toggle, boolean defaultSetting) {
        return getUnleash().isEnabled(toggle, defaultSetting);
    }

    @Override
    public List<String> getFeatureToggleNames() {
        return getUnleash().getFeatureToggleNames();
    }

    private Unleash getUnleash() {
        if (unleash == null) {
            synchronized (this) {
                unleash = new ToggleConfig().unleash();
            }
        }
        return unleash;
    }
}