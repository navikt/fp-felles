package no.nav.vedtak.felles.integrasjon.unleash;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.finn.unleash.MoreOperations;
import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.Variant;

@ApplicationScoped
public class UnleashTjeneste implements Unleash {

    private Unleash unleash;

    UnleashTjeneste() {
    }

    @Override
    public MoreOperations more() {
        return getUnleash().more();
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
        throw new RuntimeException(
                "Trenger du virkelig å hente opp alle feature toggles definert i unleash? Dette vil ødelegge statistikk globalt..");
    }

    private Unleash getUnleash() {
        if (unleash == null) {
            synchronized (this) {
                unleash = new ToggleConfig().unleash();
            }
        }
        return unleash;
    }

    @Override
    public Variant getVariant(String arg0) {
        return getUnleash().getVariant(arg0);
    }

    @Override
    public Variant getVariant(String arg0, UnleashContext arg1) {
        return getUnleash().getVariant(arg0, arg1);
    }

    @Override
    public Variant getVariant(String arg0, Variant arg1) {
        return getUnleash().getVariant(arg0, arg1);
    }

    @Override
    public Variant getVariant(String arg0, UnleashContext arg1, Variant arg2) {
        return getUnleash().getVariant(arg0, arg1, arg2);
    }
}
