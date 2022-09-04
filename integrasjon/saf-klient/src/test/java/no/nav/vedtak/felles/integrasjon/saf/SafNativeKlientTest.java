package no.nav.vedtak.felles.integrasjon.saf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.saf.AvsenderMottakerResponseProjection;
import no.nav.saf.BrukerResponseProjection;
import no.nav.saf.DokumentInfoResponseProjection;
import no.nav.saf.Dokumentoversikt;
import no.nav.saf.DokumentoversiktFagsakQueryRequest;
import no.nav.saf.DokumentoversiktFagsakQueryResponse;
import no.nav.saf.DokumentoversiktResponseProjection;
import no.nav.saf.DokumentvariantResponseProjection;
import no.nav.saf.FagsakInput;
import no.nav.saf.Journalpost;
import no.nav.saf.JournalpostQueryRequest;
import no.nav.saf.JournalpostQueryResponse;
import no.nav.saf.JournalpostResponseProjection;
import no.nav.saf.LogiskVedleggResponseProjection;
import no.nav.saf.RelevantDatoResponseProjection;
import no.nav.saf.SakResponseProjection;
import no.nav.saf.Tilknytning;
import no.nav.saf.TilknyttedeJournalposterQueryRequest;
import no.nav.saf.TilknyttedeJournalposterQueryResponse;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.RestKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class SafNativeKlientTest {

    private SafNativeTjeneste safTjeneste;

    @Mock
    private RestKlient restKlient;

    URI endpoint = URI.create("http://dummyendpoint/graphql");

    private static class SafRequest extends RestRequest {
        private SafRequest() {
            super(new TestContextSupplier());
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        // Service setup
        MDCOperations.putCallId();
        safTjeneste = new SafNativeTjeneste(restKlient, new SafRequest(), endpoint);
    }

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_dokumentoversikt_fagsak() throws IOException {
        // query-eksempel: dokumentoversiktFagsak(fagsak: {fagsakId: "2019186111",
        // fagsaksystem: "AO01"}, foerste: 5)
        var resource = getClass().getClassLoader().getResource("saf/documentResponse.json");
        var response = DefaultJsonMapper.fromJson(resource, DokumentoversiktFagsakQueryResponse.class);
        var captor = ArgumentCaptor.forClass(HttpRequest.class);
        when(restKlient.send(captor.capture(), any())).thenReturn(response);

        var query = new DokumentoversiktFagsakQueryRequest();
        query.setFagsak(new FagsakInput("fagsakId", "fagsaksystem"));
        query.setFoerste(1000);
        DokumentoversiktResponseProjection projection = byggDokumentoversiktResponseProjection();

        Dokumentoversikt dokumentoversiktFagsak = safTjeneste.dokumentoversiktFagsak(query, projection);

        assertThat(dokumentoversiktFagsak.getJournalposter()).isNotEmpty();
        var rq = captor.getValue();
        assertThat(rq.headers().map().get("Authorization")).isNotEmpty();
        assertThat(rq.headers().map().get("Nav-Consumer-Id")).contains("user");
    }

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_journalpost() throws IOException {
        // query-eksempel: journalpost(journalpostId: "439560100")
        var resource = getClass().getClassLoader().getResource("saf/journalpostResponse.json");
        var response = DefaultJsonMapper.fromJson(resource, JournalpostQueryResponse.class);
        when(restKlient.send(any(HttpRequest.class), any())).thenReturn(response);

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
        var resource = getClass().getClassLoader().getResource("saf/tilknyttetResponse.json");
        var response = DefaultJsonMapper.fromJson(resource, TilknyttedeJournalposterQueryResponse.class);
        when(restKlient.send(any(HttpRequest.class), any())).thenReturn(response);

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
        var resource = getClass().getClassLoader().getResource("saf/errorResponse.json");
        var response = DefaultJsonMapper.fromJson(resource, JournalpostQueryResponse.class);
        when(restKlient.send(any(HttpRequest.class), any())).thenReturn(response);

        var query = new JournalpostQueryRequest();
        query.setJournalpostId("journalpostId");
        var projection = byggJournalpostResponseProjection();

        assertThrows(TekniskException.class, () -> safTjeneste.hentJournalpostInfo(query, projection));
    }

    @Test
    void skal_returnere_dokument() throws IOException {
        // GET-eksempel: hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}
        Optional<byte[]> respons = Optional.of("<dokument_as_bytes>".getBytes());
        var captor = ArgumentCaptor.forClass(HttpRequest.class);
        when(restKlient.sendHandleResponse(captor.capture())).thenReturn(respons);
        HentDokumentQuery query = new HentDokumentQuery("journalpostId", "dokumentInfoId", "ARKIVF");

        byte[] dokument = safTjeneste.hentDokument(query);

        assertThat(dokument).isEqualTo("<dokument_as_bytes>".getBytes());
        var rq = captor.getValue();
        assertThat(rq.uri().toString()).contains("hentdokument/journalpostId/dokumentInfoId/ARKIVF");
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
