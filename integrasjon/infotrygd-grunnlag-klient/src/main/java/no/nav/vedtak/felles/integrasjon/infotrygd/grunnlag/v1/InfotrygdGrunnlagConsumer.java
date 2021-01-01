package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.http.client.utils.URIBuilder;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@Deprecated(since = "3.0.x", forRemoval = true)
public abstract class InfotrygdGrunnlagConsumer {

    private OidcRestClient restClient;
    private URI uri;

    public InfotrygdGrunnlagConsumer(OidcRestClient restClient, URI uri) {
        this.restClient = restClient;
        this.uri = uri;
    }

    public InfotrygdGrunnlagConsumer() {
    }

    public List<Grunnlag> getGrunnlag(String fnr, LocalDate fom) throws Exception {
        return getGrunnlag(fnr, fom, LocalDate.now());
    }

    public List<Grunnlag> getGrunnlag(String fnr, LocalDate fom, LocalDate tom) throws Exception {
        Objects.requireNonNull(fnr);
        var request = new URIBuilder(uri)
                .addParameter("fnr", fnr)
                .addParameter("fom", konverter(fom))
                .addParameter("tom", konverter(tom)).build();
        var grunnlag = restClient.get(request, Grunnlag[].class);

        return Arrays.asList(grunnlag);
    }

    private static String konverter(LocalDate dato) {
        return dato.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[restClient=" + restClient + ", uri=" + uri + "]";
    }
}
