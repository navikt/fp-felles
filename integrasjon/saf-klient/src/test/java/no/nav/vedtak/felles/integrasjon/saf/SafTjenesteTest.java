package no.nav.vedtak.felles.integrasjon.saf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.integrasjon.saf.graphql.DokumentoversiktFagsakQuery;
import no.nav.vedtak.felles.integrasjon.saf.graphql.HentDokumentQuery;
import no.nav.vedtak.felles.integrasjon.saf.graphql.JournalpostQuery;
import no.nav.vedtak.felles.integrasjon.saf.graphql.TilknyttedeJournalposterQuery;
import no.nav.vedtak.felles.integrasjon.saf.rest.model.Journalpost;
import no.nav.vedtak.felles.integrasjon.saf.rest.model.VariantFormat;

public class SafTjenesteTest {

    private SafTjeneste safTjeneste;

    private OidcRestClient restClient;
    private CloseableHttpResponse response;
    private HttpEntity entity;


    @SuppressWarnings("resource")
    @Before
    public void setUp() throws IOException {
        // Service setup
        restClient = mock(OidcRestClient.class);
        URI endpoint = URI.create("dummyendpoint/graphql");
        safTjeneste = new SafTjeneste(endpoint, restClient);

        // Mock http behavior
        // Client Setup
        response = mock(CloseableHttpResponse.class);
        entity = mock(HttpEntity.class);

        // GET mock
        when(restClient.execute(any(HttpGet.class))).thenReturn(response);

        // POST mock
        when(restClient.execute(any(HttpPost.class))).thenReturn(response);

        // response mock
        when(response.getEntity()).thenReturn(entity);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));
    }


    @SuppressWarnings("resource")
    @Test
    public void skal_returnere_dokumentoversikt_fagsak() throws IOException {
        //query-eksempel: dokumentoversiktFagsak(fagsak: {fagsakId: "2019186111", fagsaksystem: "AO01"}, foerste: 5)
        DokumentoversiktFagsakQuery query = new DokumentoversiktFagsakQuery("fagsakId", "fagsystem");
        when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("saf/documentResponse.json"));

        var dokumentoversiktFagsak = safTjeneste.dokumentoversiktFagsak(query);

        assertThat(dokumentoversiktFagsak.getJournalposter()).isNotEmpty();
    }

    @SuppressWarnings("resource")
    @Test
    public void skal_returnere_journalpost() throws IOException {
        // query-eksempel: journalpost(journalpostId: "439560100")
        JournalpostQuery query = new JournalpostQuery("journalpostId");
        when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("saf/journalpostResponse.json"));

        Journalpost journalpost = safTjeneste.hentJournalpostInfo(query);

        assertThat(journalpost.getJournalpostId()).isNotEmpty();
    }

    @SuppressWarnings("resource")
    @Test
    public void skal_returnere_tilknyttet_journalpost() throws IOException {
        // tilknyttedeJournalposter(dokumentInfoId:"469211538", tilknytning:GJENBRUK)
        var query = new TilknyttedeJournalposterQuery("dokumentInfoId");
        when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("saf/tilknyttetResponse.json"));

        List<Journalpost> journalposter = safTjeneste.hentTilknyttedeJournalposter(query);

        assertThat(journalposter).hasSize(2);
        assertThat(journalposter.stream().map(Journalpost::getEksternReferanseId).filter(Objects::nonNull).findFirst()).isPresent();
    }

    @SuppressWarnings("resource")
    @Test
    public void skal_returnere_dokument() throws IOException {
        // GET-eksempel: hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}
        HentDokumentQuery query = new HentDokumentQuery("journalpostId", "dokumentInfoId", VariantFormat.ARKIV.name());
        when(entity.getContent()).thenReturn(new ByteArrayInputStream("<dokument_as_bytes>".getBytes()));

        byte[] dokument = safTjeneste.hentDokument(query);

        assertThat(dokument).isEqualTo("<dokument_as_bytes>".getBytes());
    }
}
