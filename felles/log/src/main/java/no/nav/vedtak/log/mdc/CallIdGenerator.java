package no.nav.vedtak.log.mdc;

import java.util.UUID;

final class CallIdGenerator {

    private CallIdGenerator() {

    }

    static String create() {
        return UUID.randomUUID().toString();
    }
}
