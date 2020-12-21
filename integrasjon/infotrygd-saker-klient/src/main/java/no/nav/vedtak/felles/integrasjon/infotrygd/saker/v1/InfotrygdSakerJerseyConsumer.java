package no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.time.LocalDate;

import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOIDCClient;

public abstract class InfotrygdSakerJerseyConsumer extends AbstractJerseyOIDCClient {

    private final URI uri;

    public InfotrygdSakerJerseyConsumer(URI uri) {
        this.uri = uri;
    }

    public Saker getSaker(String fnr, LocalDate fom) throws Exception {
        return client.target(uri)
                .queryParam("fnr", fnr) // TODO bli kvitt fnr i URL
                .queryParam("fom", fom(fom))
                .request(APPLICATION_JSON)
                .get(Saker.class);
    }

    private static String fom(LocalDate fom) {
        return ISO_LOCAL_DATE.format(fom);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[uri=" + uri + ", uri=" + uri + "]";
    }

}
