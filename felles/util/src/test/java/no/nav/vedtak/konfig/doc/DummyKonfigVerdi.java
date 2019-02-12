package no.nav.vedtak.konfig.doc;

import no.nav.vedtak.konfig.KonfigVerdi;

@SuppressWarnings("unused")
public class DummyKonfigVerdi {

    @KonfigVerdi("test.felt")
    private String konfigVerdi;

    private String ikkeKonfigVerdi;

    public DummyKonfigVerdi() {
    }

    public DummyKonfigVerdi(@KonfigVerdi("test.ctor") String annenKonfigVerdi) {
    }
}
