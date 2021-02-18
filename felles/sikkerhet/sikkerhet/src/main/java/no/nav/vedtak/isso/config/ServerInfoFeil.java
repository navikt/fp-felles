package no.nav.vedtak.isso.config;

import no.nav.vedtak.exception.TekniskException;

class ServerInfoFeil {

    private ServerInfoFeil() {

    }

    static TekniskException manglerNødvendigSystemProperty(String key) {
        return new TekniskException("F-720999", String.format("Mangler nødvendig system property '%s'", key));
    }

    static TekniskException ugyldigSystemProperty(String key, String value) {
        return new TekniskException("F-836622", String.format("Ugyldig system property '%s'='%s'", key, value));
    }

    static TekniskException uventetHostFormat(String host) {
        return new TekniskException("F-050157", String.format(
                "Uventet format for host, klarer ikke å utvide cookie domain. Forventet format var xx.xx.xx, fikk '%s'. (OK hvis kjører lokalt).",
                host));
    }
}
