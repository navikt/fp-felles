package no.nav.vedtak.felles.integrasjon.saf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class SafKlientTest {

    private Saf safTjeneste;

    @Mock
    private RestClient restKlient;

    @BeforeEach
    void setUp() {
        // Service setup
        safTjeneste = new TestSafTjeneste(restKlient);
    }

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_dokumentoversikt_fagsak() throws IOException {
        // query-eksempel: dokumentoversiktFagsak(fagsak: {fagsakId: "2019186111", fagsaksystem: "AO01"}, foerste: 5)
        var resource = getClass().getClassLoader().getResource("saf/documentResponse.json");
        var response = DefaultJsonMapper.fromJson(resource.openStream(), DokumentoversiktFagsakQueryResponse.class);
        var captor = ArgumentCaptor.forClass(RestRequest.class);
        when(restKlient.send(captor.capture(), any())).thenReturn(response);

        var query = new DokumentoversiktFagsakQueryRequest();
        query.setFagsak(new FagsakInput("fagsakId", "fagsaksystem"));
        query.setFoerste(1000);
        DokumentoversiktResponseProjection projection = byggDokumentoversiktResponseProjection();

        Dokumentoversikt dokumentoversiktFagsak = safTjeneste.dokumentoversiktFagsak(query, projection);

        assertThat(dokumentoversiktFagsak.getJournalposter()).isNotEmpty();
        var rq = captor.getValue();
        assertThat(rq.validateDelayedHeaders(Set.of("Authorization", "Nav-Consumer-Id"))).isTrue();
    }


    @SuppressWarnings("resource")
    @Test
    void skal_returnere_journalpost() throws IOException {
        // query-eksempel: journalpost(journalpostId: "439560100")
        var resource = getClass().getClassLoader().getResource("saf/journalpostResponse.json");
        var response = DefaultJsonMapper.fromJson(resource.openStream(), JournalpostQueryResponse.class);
        when(restKlient.send(any(RestRequest.class), any())).thenReturn(response);

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
        var response = DefaultJsonMapper.fromJson(resource.openStream(), TilknyttedeJournalposterQueryResponse.class);
        when(restKlient.send(any(RestRequest.class), any())).thenReturn(response);

        var query = new TilknyttedeJournalposterQueryRequest();
        query.setDokumentInfoId("dokumentInfoId");
        query.setTilknytning(Tilknytning.GJENBRUK);
        var projection = new JournalpostResponseProjection().journalpostId().eksternReferanseId();

        List<Journalpost> journalposter = safTjeneste.hentTilknyttedeJournalposter(query, projection);

        assertThat(journalposter).hasSize(2);
        assertThat(journalposter.stream().map(Journalpost::getEksternReferanseId).filter(Objects::nonNull).findFirst()).isPresent();
    }

    @SuppressWarnings("resource")
    @Test
    void skal_konvertere_feilmelding_til_feil() throws IOException {
        // query-eksempel: journalpost(journalpostId: "439560100")
        var resource = getClass().getClassLoader().getResource("saf/errorResponse.json");
        var response = DefaultJsonMapper.fromJson(resource.openStream(), JournalpostQueryResponse.class);
        when(restKlient.send(any(RestRequest.class), any())).thenReturn(response);

        var query = new JournalpostQueryRequest();
        query.setJournalpostId("journalpostId");
        var projection = byggJournalpostResponseProjection();

        assertThrows(TekniskException.class, () -> safTjeneste.hentJournalpostInfo(query, projection));
    }

    @Test
    void skal_returnere_dokument() {
        // GET-eksempel: hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}
        byte[] respons = "<dokument_as_bytes>".getBytes();
        var captor = ArgumentCaptor.forClass(RestRequest.class);
        when(restKlient.sendReturnByteArray(captor.capture())).thenReturn(respons);
        HentDokumentQuery query = new HentDokumentQuery("journalpostId", "dokumentInfoId", "ARKIVF");

        byte[] dokument = safTjeneste.hentDokument(query);

        assertThat(dokument).isEqualTo("<dokument_as_bytes>".getBytes());
        var rq = captor.getValue();
        rq.validateRequest(r -> assertThat(r.uri().toString()).contains("hentdokument/journalpostId/dokumentInfoId/ARKIVF"));
    }

    private DokumentoversiktResponseProjection byggDokumentoversiktResponseProjection() {
        return new DokumentoversiktResponseProjection().journalposter(new JournalpostResponseProjection().journalpostId()
            .tittel()
            .journalposttype()
            .journalstatus()
            .kanal()
            .tema()
            .behandlingstema()
            .sak(new SakResponseProjection().arkivsaksnummer().arkivsaksystem().fagsaksystem().fagsakId())
            .bruker(new BrukerResponseProjection().id().type())
            .avsenderMottaker(new AvsenderMottakerResponseProjection().id().type().navn())
            .journalfoerendeEnhet()
            .dokumenter(new DokumentInfoResponseProjection().dokumentInfoId()
                .tittel()
                .brevkode()
                .dokumentvarianter(new DokumentvariantResponseProjection().variantformat().filnavn().filtype().saksbehandlerHarTilgang())
                .logiskeVedlegg(new LogiskVedleggResponseProjection().tittel()))
            .datoOpprettet()
            .relevanteDatoer(new RelevantDatoResponseProjection().dato().datotype())
            .eksternReferanseId());
    }

    private JournalpostResponseProjection byggJournalpostResponseProjection() {
        return new JournalpostResponseProjection().journalpostId()
            .tittel()
            .journalposttype()
            .journalstatus()
            .kanal()
            .tema()
            .behandlingstema()
            .sak(new SakResponseProjection().arkivsaksnummer().arkivsaksystem().fagsaksystem().fagsakId())
            .bruker(new BrukerResponseProjection().id().type())
            .avsenderMottaker(new AvsenderMottakerResponseProjection().id().type().navn())
            .journalfoerendeEnhet()
            .dokumenter(new DokumentInfoResponseProjection().dokumentInfoId()
                .tittel()
                .brevkode()
                .dokumentvarianter(new DokumentvariantResponseProjection().variantformat().filnavn())
                .logiskeVedlegg(new LogiskVedleggResponseProjection().tittel()))
            .datoOpprettet()
            .relevanteDatoer(new RelevantDatoResponseProjection().dato().datotype())
            .eksternReferanseId();
    }

    @RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "saf.base.url", endpointDefault = "https://saf.nais.adeo.no", scopesProperty = "saf.scopes", scopesDefault = "api://prod-fss.teamdokumenthandtering.saf/.default")
    private static class TestSafTjeneste extends AbstractSafKlient {
        TestSafTjeneste(RestClient restKlient) {
            super(restKlient);
        }
    }
}
