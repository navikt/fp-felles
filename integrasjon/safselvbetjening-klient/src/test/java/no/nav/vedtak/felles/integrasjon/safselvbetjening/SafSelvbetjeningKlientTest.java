package no.nav.vedtak.felles.integrasjon.safselvbetjening;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Set;

import no.nav.safselvbetjening.AvsenderMottakerResponseProjection;
import no.nav.safselvbetjening.DokumentInfoResponseProjection;
import no.nav.safselvbetjening.DokumentoversiktResponseProjection;
import no.nav.safselvbetjening.DokumentoversiktSelvbetjeningQueryRequest;
import no.nav.safselvbetjening.DokumentoversiktSelvbetjeningQueryResponse;
import no.nav.safselvbetjening.DokumentvariantResponseProjection;
import no.nav.safselvbetjening.JournalpostResponseProjection;
import no.nav.safselvbetjening.RelevantDatoResponseProjection;
import no.nav.safselvbetjening.SakResponseProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class SafSelvbetjeningKlientTest {

    private SafSelvbetjening safSelvbetjeningTjeneste;

    @Mock
    private RestClient restKlient;

    @BeforeEach
    void setUp() {
        // Service setup
        safSelvbetjeningTjeneste = new SafSelvbetjeningKlient(restKlient);
    }

    @Test
    void skal_returnere_dokumentoversikt() {
        var response = """
                        {
                          "data": {
                            "dokumentoversiktSelvbetjening": {
                              "journalposter": [
                                {
                                  "journalpostId": "464053024",
                                  "eksternReferanseId": null,
                                  "dokumenter": [
                                    {
                                      "dokumentvarianter": [
                                        {
                                          "saksbehandlerHarTilgang": false
                                        }
                                      ]
                                    }
                                  ]
                                },
                                {
                                  "journalpostId": "460025122",
                                  "eksternReferanseId": "s3010190002NAV988024.pdf",
                                  "dokumenter": [
                                    {
                                      "dokumentvarianter": [
                                        {
                                          "saksbehandlerHarTilgang": false
                                        }
                                      ]
                                    }
                                  ]
                                }
                              ]
                            }
                          }
                        }
                """;
        var captor = ArgumentCaptor.forClass(RestRequest.class);
        when(restKlient.send(captor.capture(), any())).thenReturn(DefaultJsonMapper.fromJson(response, DokumentoversiktSelvbetjeningQueryResponse.class));

        var query = new DokumentoversiktSelvbetjeningQueryRequest();
        var projection = byggDokumentoversiktResponseProjection();

        var dokumentoversikt = safSelvbetjeningTjeneste.dokumentoversiktFagsak(query, projection);

        assertThat(dokumentoversikt.getJournalposter()).hasSize(2);
        var rq = captor.getValue();
        assertThat(rq.validateDelayedHeaders(Set.of("Authorization", "Nav-Consumer-Id"))).isTrue();
    }

    @Test
    void skal_returnere_dokument() {
        byte[] respons = "<dokument_as_bytes>".getBytes();
        var captor = ArgumentCaptor.forClass(RestRequest.class);
        when(restKlient.sendReturnByteArray(captor.capture())).thenReturn(respons);
        HentDokumentQuery query = new HentDokumentQuery("journalpostId", "dokumentInfoId");

        byte[] dokument = safSelvbetjeningTjeneste.hentDokument(query);

        assertThat(dokument).isEqualTo("<dokument_as_bytes>".getBytes());
        var rq = captor.getValue();
        rq.validateRequest(r -> assertThat(r.uri().toString()).contains("hentdokument/journalpostId/dokumentInfoId/ARKIV"));
    }

    private DokumentoversiktResponseProjection byggDokumentoversiktResponseProjection() {
        return new DokumentoversiktResponseProjection().journalposter(new JournalpostResponseProjection().journalpostId()
            .tittel()
            .journalposttype()
            .journalstatus()
            .kanal()
            .tema()
            .sak(new SakResponseProjection().fagsakId().sakstype())
            .avsender(new AvsenderMottakerResponseProjection().id().type().navn())
            .dokumenter(new DokumentInfoResponseProjection().dokumentInfoId()
                .tittel()
                .brevkode()
                .dokumentInfoId()
                .dokumentvarianter(new DokumentvariantResponseProjection().variantformat().filtype().brukerHarTilgang()))
            .relevanteDatoer(new RelevantDatoResponseProjection().dato().datotype())
            .eksternReferanseId());
    }
}
