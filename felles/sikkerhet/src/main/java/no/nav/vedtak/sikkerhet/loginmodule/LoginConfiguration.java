package no.nav.vedtak.sikkerhet.loginmodule;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/** Pluggbart interface for å legge til login moduler vha ServiceLoader. */
public interface LoginConfiguration {

    /** Navn. Matcher name for indeksering av {@link Configuration}. */
    String getName();

    AppConfigurationEntry[] getAppConfigurationEntry();
}
