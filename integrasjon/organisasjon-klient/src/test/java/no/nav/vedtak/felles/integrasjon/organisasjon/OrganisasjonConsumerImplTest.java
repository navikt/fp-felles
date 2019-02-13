package no.nav.vedtak.felles.integrasjon.organisasjon;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumerImpl;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class OrganisasjonConsumerImplTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private OrganisasjonV4 port;
    private OrganisasjonConsumerImpl consumer;

    @Before
    public void setup() {
        port = mock(OrganisasjonV4.class);
        consumer = new OrganisasjonConsumerImpl(port);
    }

    @Test
    public void skal_kalle_ws_for_å_hente_organisasjon() throws Exception {
        // Arrange
        no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse hentOrganisasjonResponse =
                new no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse();
        when(port.hentOrganisasjon(any(no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest.class))).thenReturn(hentOrganisasjonResponse);

        // Act
        HentOrganisasjonRequest request = new HentOrganisasjonRequest("918450165");
        @SuppressWarnings("unused")
        HentOrganisasjonResponse response = consumer.hentOrganisasjon(request);

        // Assert
        verify(port, times(1)).hentOrganisasjon(any(no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest.class));
    }

}