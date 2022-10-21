package no.nav.vedtak.sikkerhet.loginmodule.saml;

/**
* Contains info from SAML token
*/
record SamlInfo(String uid, String identType, int authLevel, String consumerId) {
}
