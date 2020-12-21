package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOIDCClient;

public abstract class InfotrygdGrunnlagJerseyConsumer extends AbstractJerseyOIDCClient {

    private final URI uri;

    public InfotrygdGrunnlagJerseyConsumer(URI uri) {
        this.uri = uri;
    }

    public List<Grunnlag> getGrunnlag(String fnr, LocalDate fom) throws Exception {
        return getGrunnlag(fnr, fom, LocalDate.now());
    }

    public List<Grunnlag> getGrunnlag(String fnr, LocalDate fom, LocalDate tom) throws Exception {
        return client.target(uri)
                .queryParam("fnr", fnr) // TODO bli kvitt fnr i URL
                .queryParam("fom", konverter(fom))
                .queryParam("tom", konverter(tom))
                .request(APPLICATION_JSON)
                .get(Response.class)
                .readEntity(new GenericType<List<Grunnlag>>() {
                });
    }

    private static String konverter(LocalDate dato) {
        return dato.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[uri=" + uri + "]";
    }
}
