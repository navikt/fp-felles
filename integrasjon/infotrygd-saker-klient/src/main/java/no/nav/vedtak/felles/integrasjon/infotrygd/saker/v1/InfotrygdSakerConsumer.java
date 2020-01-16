package no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.apache.http.client.utils.URIBuilder;

import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public abstract class InfotrygdSakerConsumer {

    private OidcRestClient restClient;
    private URI uri;

    public InfotrygdSakerConsumer(OidcRestClient restClient, URI uri) {
        this.restClient = restClient;
        this.uri = uri;
    }

    public InfotrygdSakerConsumer() {
        // Tja, nødvendig her?
    }

    public Saker getSaker(String fnr, LocalDate fom) throws Exception {
        Objects.requireNonNull(fnr);
        var request = new URIBuilder(uri)
            .addParameter("fnr", fnr)
            .addParameter("fom", fom(fom))
            .build();
        return restClient.get(request, Saker.class);
    }

    private static String fom(LocalDate fom) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(fom);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[restClient=" + restClient + ", uri=" + uri + "]";
    }

}
