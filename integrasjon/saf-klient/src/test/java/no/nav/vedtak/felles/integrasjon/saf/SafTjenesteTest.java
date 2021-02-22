package no.nav.vedtak.felles.integrasjon.saf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.saf.AvsenderMottakerResponseProjection;
import no.nav.saf.BrukerResponseProjection;
import no.nav.saf.DokumentInfoResponseProjection;
import no.nav.saf.Dokumentoversikt;
import no.nav.saf.DokumentoversiktFagsakQueryRequest;
import no.nav.saf.DokumentoversiktResponseProjection;
import no.nav.saf.DokumentvariantResponseProjection;
import no.nav.saf.FagsakInput;
import no.nav.saf.Journalpost;
import no.nav.saf.JournalpostQueryRequest;
import no.nav.saf.JournalpostResponseProjection;
import no.nav.saf.LogiskVedleggResponseProjection;
import no.nav.saf.RelevantDatoResponseProjection;
import no.nav.saf.SakResponseProjection;
import no.nav.saf.Tilknytning;
import no.nav.saf.TilknyttedeJournalposterQueryRequest;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class SafTjenesteTest {

    private SafTjeneste safTjeneste;

    @Mock
    private OidcRestClient restClient;
    @Mock
    private CloseableHttpResponse response;
    @Mock
    private HttpEntity entity;

    @BeforeEach
    void setUp() throws IOException {
        // Service setup
        URI endpoint = URI.create("dummyendpoint/graphql");
        safTjeneste = new SafTjeneste(endpoint, restClient);

        // Mock http behavior
        // Client Setup
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
    void skal_returnere_dokumentoversikt_fagsak() throws IOException {
        // query-eksempel: dokumentoversiktFagsak(fagsak: {fagsakId: "2019186111",
        // fagsaksystem: "AO01"}, foerste: 5)
        when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("saf/documentResponse.json"));

        var query = new DokumentoversiktFagsakQueryRequest();
        query.setFagsak(new FagsakInput("fagsakId", "fagsaksystem"));
        query.setFoerste(1000);
        DokumentoversiktResponseProjection projection = byggDokumentoversiktResponseProjection();

        Dokumentoversikt dokumentoversiktFagsak = safTjeneste.dokumentoversiktFagsak(query, projection);

        assertThat(dokumentoversiktFagsak.getJournalposter()).isNotEmpty();
    }

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_journalpost() throws IOException {
        // query-eksempel: journalpost(journalpostId: "439560100")
        when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("saf/journalpostResponse.json"));

        var query = new JournalpostQueryRequest();
        query.setJournalpostId("journalpostId");
        var projection = byggJournalpostResponseProjection();

        var journalpost = safTjeneste.hentJournalpostInfo(query, projection);

        assertThat(journalpost.getJournalpostId()).isNotEmpty();
    }

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_tilknyttet_journalpost() throws IOException {
        // query-eksempel: tilknyttedeJournalposter(dokumentInfoId:"469211538",
        // tilknytning:GJENBRUK)
        when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("saf/tilknyttetResponse.json"));

        var query = new TilknyttedeJournalposterQueryRequest();
        query.setDokumentInfoId("dokumentInfoId");
        query.setTilknytning(Tilknytning.GJENBRUK);
        var projection = new JournalpostResponseProjection()
                .journalpostId()
                .eksternReferanseId();

        List<Journalpost> journalposter = safTjeneste.hentTilknyttedeJournalposter(query, projection);

        assertThat(journalposter).hasSize(2);
        assertThat(journalposter.stream().map(Journalpost::getEksternReferanseId).filter(Objects::nonNull).findFirst()).isPresent();
    }

    @SuppressWarnings("resource")
    @Test
    void skal_konvertere_feilmelding_til_feil() throws IOException {
        // query-eksempel: journalpost(journalpostId: "439560100")
        when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("saf/errorResponse.json"));

        var query = new JournalpostQueryRequest();
        query.setJournalpostId("journalpostId");
        var projection = byggJournalpostResponseProjection();

        assertThrows(TekniskException.class, () -> safTjeneste.hentJournalpostInfo(query, projection));
    }

    @Test
    void skal_returnere_dokument() throws IOException {
        // GET-eksempel: hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}
        HentDokumentQuery query = new HentDokumentQuery("journalpostId", "dokumentInfoId", "ARKIVF");
        when(entity.getContent()).thenReturn(new ByteArrayInputStream("<dokument_as_bytes>".getBytes()));

        byte[] dokument = safTjeneste.hentDokument(query);

        assertThat(dokument).isEqualTo("<dokument_as_bytes>".getBytes());
    }

    private DokumentoversiktResponseProjection byggDokumentoversiktResponseProjection() {
        return new DokumentoversiktResponseProjection()
                .journalposter(new JournalpostResponseProjection()
                        .journalpostId()
                        .tittel()
                        .journalposttype()
                        .journalstatus()
                        .kanal()
                        .tema()
                        .behandlingstema()
                        .sak(new SakResponseProjection()
                                .arkivsaksnummer()
                                .arkivsaksystem()
                                .fagsaksystem()
                                .fagsakId())
                        .bruker(new BrukerResponseProjection()
                                .id()
                                .type())
                        .avsenderMottaker(new AvsenderMottakerResponseProjection()
                                .id()
                                .type()
                                .navn())
                        .journalfoerendeEnhet()
                        .dokumenter(new DokumentInfoResponseProjection()
                                .dokumentInfoId()
                                .tittel()
                                .brevkode()
                                .dokumentvarianter(new DokumentvariantResponseProjection()
                                        .variantformat()
                                        .filnavn()
                                        .filtype()
                                        .saksbehandlerHarTilgang())
                                .logiskeVedlegg(new LogiskVedleggResponseProjection()
                                        .tittel()))
                        .datoOpprettet()
                        .relevanteDatoer(new RelevantDatoResponseProjection()
                                .dato()
                                .datotype())
                        .eksternReferanseId());
    }

    private JournalpostResponseProjection byggJournalpostResponseProjection() {
        return new JournalpostResponseProjection()
                .journalpostId()
                .tittel()
                .journalposttype()
                .journalstatus()
                .kanal()
                .tema()
                .behandlingstema()
                .sak(new SakResponseProjection()
                        .arkivsaksnummer()
                        .arkivsaksystem()
                        .fagsaksystem()
                        .fagsakId())
                .bruker(new BrukerResponseProjection()
                        .id()
                        .type())
                .avsenderMottaker(new AvsenderMottakerResponseProjection()
                        .id()
                        .type()
                        .navn())
                .journalfoerendeEnhet()
                .dokumenter(new DokumentInfoResponseProjection()
                        .dokumentInfoId()
                        .tittel()
                        .brevkode()
                        .dokumentvarianter(new DokumentvariantResponseProjection()
                                .variantformat()
                                .filnavn())
                        .logiskeVedlegg(new LogiskVedleggResponseProjection()
                                .tittel()))
                .datoOpprettet()
                .relevanteDatoer(new RelevantDatoResponseProjection()
                        .dato()
                        .datotype())
                .eksternReferanseId();
    }
}
