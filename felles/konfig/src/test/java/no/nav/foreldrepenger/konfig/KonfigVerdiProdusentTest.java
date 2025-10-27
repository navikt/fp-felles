package no.nav.foreldrepenger.konfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


class KonfigVerdiProdusentTest {

    @Test
    void passord_skal_ikke_logges_test() {
        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("db.passord").matches());
        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("DB_PASSORD").matches());

        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("db.password").matches());
        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("DB_PASSWORD").matches());
    }

    @Test
    void credintials_skal_ikke_logges() {
        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("db.credential").matches());
        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("DB_CREDENTIAL").matches());

        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("db.kredential").matches());
        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("DB_KREDENTIAL").matches());
    }

    @Test
    void secrets_skal_ikke_logges() {
        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("db.secret").matches());
        assertTrue(KonfigVerdiProdusent.SKJUL.matcher("DB_SECRET").matches());
    }
}
