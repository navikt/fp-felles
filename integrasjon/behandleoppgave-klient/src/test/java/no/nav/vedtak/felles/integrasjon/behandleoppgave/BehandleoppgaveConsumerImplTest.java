package no.nav.vedtak.felles.integrasjon.behandleoppgave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSFerdigstillOppgaveException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSFerdigstillOppgaveRequest;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSFerdigstillOppgaveResponse;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOppgave;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveRequest;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BrukerType;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.FerdigstillOppgaveRequestMal;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumerImpl;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class BehandleoppgaveConsumerImplTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private BehandleOppgaveV1 port;
    private BehandleoppgaveConsumerImpl consumer;

    @Before
    public void setup() {
        port = mock(BehandleOppgaveV1.class);
        consumer = new BehandleoppgaveConsumerImpl(port);
    }

    @Test
    public void skal_kalle_ws_for_å_ferdigstille_oppgave() throws Exception {
        // Arrange
        WSFerdigstillOppgaveResponse ferdigstillOppgaveResponse = new WSFerdigstillOppgaveResponse();
        when(port.ferdigstillOppgave(any(WSFerdigstillOppgaveRequest.class))).thenReturn(ferdigstillOppgaveResponse);

        // Act
        WSFerdigstillOppgaveResponse response = consumer.ferdigstillOppgave(FerdigstillOppgaveRequestMal.builder().medOppgaveId("1").medFerdigstiltAvEnhetId(2).build());

        // Assert
        assertThat(response).isEqualTo(ferdigstillOppgaveResponse);
    }

    @Test
    public void skal_kaste_exception_hvis_ferdigstill_response_inneholder_feil() throws Exception {
        String feilBeskrivelse = "Ugyldig oppgaveId";

        // Arrange
        when(port.ferdigstillOppgave(any(WSFerdigstillOppgaveRequest.class))).thenThrow(new WSFerdigstillOppgaveException(feilBeskrivelse));

        // Assert
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(feilBeskrivelse);

        // Act
        consumer.ferdigstillOppgave(FerdigstillOppgaveRequestMal.builder().medOppgaveId("1").medFerdigstiltAvEnhetId(2).build());
    }

    @Test
    public void skal_mappe_alle_verdier_fra_mal_til_request() throws Exception {
        final LocalDate now = LocalDate.now();
        final OpprettOppgaveRequest mal = OpprettOppgaveRequest.builder()
                .medMottattDato(now.plusDays((long) (Math.random() * 6L)))
                .medDokumentId("dokumentId")
                .medBrukerTypeKode(BrukerType.PERSON)
                .medFnr("12345123456")
                .medUnderkategoriKode("UnderKatt")
                .medFagomradeKode("FOR")
                .medSaksnummer("Saksnr")
                .medAktivTil(now.plusDays((long) (Math.random() * 6L)))
                .medAktivFra(now.plusDays((long) (Math.random() * 6L)))
                .medAnsvarligEnhetId("EnhetId")
                .medBeskrivelse("Beskrivelse")
                .medLest(true)
                .medOppgavetypeKode("OppgaveType")
                .medPrioritetKode("HOY_FOR")
                .medNormertBehandlingsTidInnen(now.plusDays((long) (Math.random() * 6L)))
                .build();

        final WSOpprettOppgaveRequest request = consumer.convertToWSRequest(mal);
        final WSOppgave oppgave = request.getWsOppgave();

        assertThat(request.getOpprettetAvEnhetId()).isEqualTo(mal.getOpprettetAvEnhetId());
        assertThat(DateUtil.convertToLocalDate(oppgave.getAktivFra())).isEqualTo(mal.getAktivFra());
        assertThat(DateUtil.convertToLocalDate(oppgave.getAktivTil())).isEqualTo(mal.getAktivTil().get());
        assertThat(oppgave.getGjelderBruker().getIdent()).isEqualTo(mal.getFnr());
//        assertThat(oppgave.getGjelderBruker().getAktorType()).isEqualTo(mal.getBrukerTypeKode().name()); TODO (Stig) fixme
        assertThat(oppgave.getBeskrivelse()).isEqualTo(mal.getBeskrivelse());
        assertThat(oppgave.getFagomradeKode()).isEqualTo(mal.getFagomradeKode().name());
        assertThat(DateUtil.convertToLocalDate(oppgave.getMottattDato())).isEqualTo(mal.getMottattDato());
        assertThat(oppgave.getUnderkategoriKode()).isEqualTo(mal.getUnderkategoriKode());
        assertThat(DateUtil.convertToLocalDate(oppgave.getNormDato())).isEqualTo(mal.getNormertBehandlingsTidInnen());
        assertThat(oppgave.getPrioritetKode()).isEqualTo(mal.getPrioritetKode().name());
        assertThat(oppgave.getAnsvarligEnhetId()).isEqualTo(mal.getAnsvarligEnhetId());
        assertThat(oppgave.getDokumentId()).isEqualTo(mal.getDokumentId());
        assertThat(oppgave.getSaksnummer()).isEqualTo(mal.getSaksnummer());
        assertThat(oppgave.isLest()).isEqualTo(mal.isLest());
    }
}