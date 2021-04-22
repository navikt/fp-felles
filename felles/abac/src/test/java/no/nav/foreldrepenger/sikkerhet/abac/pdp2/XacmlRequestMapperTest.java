package no.nav.foreldrepenger.sikkerhet.abac.pdp2;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacAttributtNøkkel;
import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacBehandlingStatus;
import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacFagsakStatus;
import no.nav.foreldrepenger.sikkerhet.abac.domene.ActionType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdSubject;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdToken;
import no.nav.foreldrepenger.sikkerhet.abac.domene.TokenType;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlRequest;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;

class XacmlRequestMapperTest {
    public static final String JWT_TOKEN = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICIyb2c1RGk5ZW9LeFhOa3VPd0dvVUdBIiwgInN1YiI6ICJzMTQyNDQzIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI1NTM0ZmQ4ZS03MmE2LTRhMWQtOWU5YS1iZmEzYThhMTljMDUtNjE2NjA2NyIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJPSURDIiwgImNfaGFzaCI6ICJiVWYzcU5CN3dTdi0wVlN0bjhXLURnIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiMTdhOGZiMzYtMGI0Ny00YzRkLWE4YWYtZWM4Nzc3Y2MyZmIyIiwgImF6cCI6ICJPSURDIiwgImF1dGhfdGltZSI6IDE0OTgwMzk5MTQsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE0OTgwNDM1MTUsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTQ5ODAzOTkxNSB9.S2DKQweQWZIfjaAT2UP9_dxrK5zqpXj8IgtjDLt5PVfLYfZqpWGaX-ckXG0GlztDVBlRK4ylmIYacTmEAUV_bRa_qWKRNxF83SlQRgHDSiE82SGv5WHOGEcAxf2w_d50XsgA2KDBCyv0bFIp9bCiKzP11uWPW0v4uIkyw2xVxMVPMCuiMUtYFh80sMDf9T4FuQcFd0LxoYcSFDEDlwCdRiF3ufw73qtMYBlNIMbTGHx-DZWkZV7CgukmCee79gwQIvGwdLrgaDrHFCJUDCbB1FFEaE3p3_BZbj0T54fCvL69aHyWm1zEd9Pys15yZdSh3oSSr4yVNIxhoF-nQ7gY-g;";

    @Test
    void testMapper() {
        var request = NyXacmlRequestMapper.lagXacmlRequest(lagPdpRequestBuilder().build());
        validerBasisActionAttributter(request);
        validerBasisEnvironmentAttributter(request);
        validerBasisResourceAttributter(request, 1, 2);
        assertThat(request.getRequest().getAccessSubject()).isNull();
    }

    @Test
    @DisplayName("TokenX token - ikke set token men bruk subject info isteden.")
    @Disabled
    void testMapperMedTokenX() {
        var pdpRequest = lagPdpRequestBuilder(IdToken.withToken(JWT_TOKEN, TokenType.TOKENX)).build();
        pdpRequest.setIdSubject(IdSubject.with("srvTest", "EksternBruker", 3));
        var request = NyXacmlRequestMapper.lagXacmlRequest(pdpRequest);

        validerBasisActionAttributter(request);

        assertThat(request.getRequest().getEnvironment()).isNotNull();
        assertThat(request.getRequest().getEnvironment().getAttribute()).isNotNull();
        assertThat(request.getRequest().getEnvironment().getAttribute()).hasSize(1);
        assertThat(request.getRequest().getEnvironment().getAttribute().get(0).getAttributeId()).isEqualTo(AbacAttributtNøkkel.ENVIRONMENT_PEP_ID);
        assertThat(request.getRequest().getEnvironment().getAttribute().get(0).getValue()).isEqualTo(null);

        validerBasisResourceAttributter(request, 1, 2);

        validerBasisSubjectAttributter(request, 3);
        assertThat(request.getRequest().getAccessSubject().getAttribute().get(2).getAttributeId()).isEqualTo(AbacAttributtNøkkel.SUBJECT_LEVEL);
        assertThat(request.getRequest().getAccessSubject().getAttribute().get(2).getValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Not TokenX - set token og ignorer subject.")
    void testMapperMedUtenTokenX() {
        var pdpRequest = lagPdpRequestBuilder().build();
        pdpRequest.setIdSubject(IdSubject.with("srvTest", "EksternBruker", 3));
        var request = NyXacmlRequestMapper.lagXacmlRequest(pdpRequest);

        validerBasisActionAttributter(request);
        validerBasisEnvironmentAttributter(request);
        validerBasisResourceAttributter(request, 1, 2);

        assertThat(request.getRequest()).isNotNull();
        assertThat(request.getRequest().getAccessSubject()).isNull();
    }

    @Test
    void testMapperMedToAktørIder() {
        var pdpRequest = lagPdpRequestBuilder().build();
        var aktørIder = Set.of("1234", "5678");
        pdpRequest.setAktørIder(aktørIder);
        var request = NyXacmlRequestMapper.lagXacmlRequest(pdpRequest);

        validerBasisActionAttributter(request);
        validerBasisEnvironmentAttributter(request);
        validerBasisResourceAttributter(request, 2, 3);
        validerResourceAttributter(aktørIder, request, AbacAttributtNøkkel.RESOURCE_PERSON_AKTOERID);
    }

    @Test
    void testMapperMedToFnrOgToAktørIder() {
        var pdpRequest = lagPdpRequestBuilder().build();
        var aktørIder = Set.of("1234", "5678");
        pdpRequest.setAktørIder(aktørIder);
        var personnummere = Set.of("12345678", "09876543");
        pdpRequest.setPersonnummere(personnummere);
        var request = NyXacmlRequestMapper.lagXacmlRequest(pdpRequest);

        validerBasisActionAttributter(request);
        validerBasisEnvironmentAttributter(request);
        validerBasisResourceAttributter(request, 4, 3);

        validerResourceAttributter(aktørIder, request, AbacAttributtNøkkel.RESOURCE_PERSON_AKTOERID);
        validerResourceAttributter(personnummere, request, AbacAttributtNøkkel.RESOURCE_PERSON_FNR);
    }

    @Test
    void testMapperMedToFnrOgToAktørIderToAksjonspunkter() {
        var pdpRequest = lagPdpRequestBuilder().build();
        var aktørIder = Set.of("1234", "5678");
        pdpRequest.setAktørIder(aktørIder);
        var personnummere = Set.of("12345678", "09876543");
        pdpRequest.setPersonnummere(personnummere);
        var aksjonspunkter = Set.of("5080", "5074");
        pdpRequest.setAksjonspunkter(aksjonspunkter);
        var request = NyXacmlRequestMapper.lagXacmlRequest(pdpRequest);

        validerBasisActionAttributter(request);
        validerBasisEnvironmentAttributter(request);
        validerBasisResourceAttributter(request, 8, 4);
        validerResourceAttributter(aktørIder, request, AbacAttributtNøkkel.RESOURCE_PERSON_AKTOERID);
        validerResourceAttributter(personnummere, request, AbacAttributtNøkkel.RESOURCE_PERSON_FNR);
        validerResourceAttributter(aksjonspunkter, request, AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE);
    }

    @Test
    void testMapperMedAlleResurserSatt() {
        var pepId = "testPepId";
        var domene = "foreldre";
        var resourceType = "no.nav.abac.attributter.foreldrepenger.fagsak";
        var pdpRequest = lagPdpRequestBuilder()
            .medPepId(pepId)
            .medDomene(domene)
            .medResourceType(resourceType)
            .medIdToken(IdToken.withToken("SAML", TokenType.SAML))
            .medUserId("srvTest")
            .build();
        var aktørIder = Set.of("1234");
        pdpRequest.setAktørIder(aktørIder);
        var personnummere = Set.of("12345678");
        pdpRequest.setPersonnummere(personnummere);
        var aksjonspunkter = Set.of("5080");
        pdpRequest.setAksjonspunkter(aksjonspunkter);
        pdpRequest.setAleneomsorg(true);
        var annenPartAktørId = "09876543";
        pdpRequest.setAnnenPartAktørId(annenPartAktørId);
        var ansvarligSaksbenandler = "Katarzyna";
        pdpRequest.setAnsvarligSaksbenandler(ansvarligSaksbenandler);
        pdpRequest.setBehandlingStatus(AbacBehandlingStatus.OPPRETTET);
        pdpRequest.setFagsakStatus(AbacFagsakStatus.OPPRETTET);
        var request = NyXacmlRequestMapper.lagXacmlRequest(pdpRequest);

        validerBasisActionAttributter(request);
        validerBasisEnvironmentAttributter(request, pepId, base64encode("SAML"), AbacAttributtNøkkel.ENVIRONMENT_SAML_TOKEN);
        validerBasisResourceAttributter(request, 2, 9);
        validerResourceAttributter(aktørIder, request, AbacAttributtNøkkel.RESOURCE_PERSON_AKTOERID);
        validerAleneomsorg(Boolean.TRUE, request);
        validerResourceAttributter(personnummere, request, AbacAttributtNøkkel.RESOURCE_PERSON_FNR);
        validerResourceAttributter(aksjonspunkter, request, AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE);
        validerResourceAttributter(Set.of(domene), request, AbacAttributtNøkkel.RESOURCE_DOMENE);
        validerResourceAttributter(Set.of(resourceType), request, AbacAttributtNøkkel.RESOURCE_RESOURCE_TYPE);
        validerResourceAttributter(Set.of(annenPartAktørId), request, AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_ANNEN_PART);
        validerResourceAttributter(Set.of(ansvarligSaksbenandler), request, AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER);
        validerResourceAttributter(Set.of(AbacBehandlingStatus.OPPRETTET.getEksternKode()), request, AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS);
        validerResourceAttributter(Set.of(AbacFagsakStatus.OPPRETTET.getEksternKode()), request, AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS);
    }

    private void validerResourceAttributter(final Set<String> expectedValues, final XacmlRequest request, String attributtKey) {
        var resultatVerdier = request.getRequest().getResource()
            .stream()
            .flatMap(it -> it.getAttribute().stream()
                .filter(s -> s.getAttributeId().equals(attributtKey))
                .map(XacmlRequest.Pair::getValue))
            .collect(Collectors.toSet());
        assertThat(resultatVerdier).containsAll(expectedValues);
    }

    private void validerAleneomsorg(final Boolean expected, final XacmlRequest request) {
        var resultatVerdier = request.getRequest().getResource()
            .stream()
            .flatMap(it -> it.getAttribute().stream()
                .filter(s -> s.getAttributeId().equals(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_ALENEOMSORG))
                .map(XacmlRequest.Pair::getValue))
            .collect(Collectors.toSet());
        assertThat(resultatVerdier).containsAll(Set.of(expected));
    }

    private void validerBasisSubjectAttributter(final XacmlRequest request, int antall) {
        assertThat(request.getRequest()).isNotNull();
        assertThat(request.getRequest().getAccessSubject()).isNotNull();
        assertThat(request.getRequest().getAccessSubject().getAttribute()).isNotNull();
        assertThat(request.getRequest().getAccessSubject().getAttribute()).hasSize(antall);
        assertThat(request.getRequest().getAccessSubject().getAttribute().get(0).getAttributeId()).isEqualTo(AbacAttributtNøkkel.SUBJECT_ID);
        assertThat(request.getRequest().getAccessSubject().getAttribute().get(0).getValue()).isEqualTo("srvTest");
        assertThat(request.getRequest().getAccessSubject().getAttribute().get(1).getAttributeId()).isEqualTo(AbacAttributtNøkkel.SUBJECT_TYPE);
        assertThat(request.getRequest().getAccessSubject().getAttribute().get(1).getValue()).isEqualTo("EksternBruker");
    }

    private void validerBasisResourceAttributter(final XacmlRequest request, int antallRessources, int antallAttributterPerRessource) {
        assertThat(request.getRequest().getResource()).isNotNull();
        assertThat(request.getRequest().getResource()).hasSize(antallRessources);
        assertThat(request.getRequest().getResource().get(0).getAttribute()).isNotNull();
        assertThat(request.getRequest().getResource().get(0).getAttribute()).hasSize(antallAttributterPerRessource);
        assertThat(request.getRequest().getResource().get(0).getAttribute().get(0).getAttributeId()).isEqualTo(AbacAttributtNøkkel.RESOURCE_DOMENE);
        assertThat(request.getRequest().getResource().get(0).getAttribute().get(0).getValue()).isEqualTo("foreldre");
        assertThat(request.getRequest().getResource().get(0).getAttribute().get(1).getAttributeId()).isEqualTo(AbacAttributtNøkkel.RESOURCE_RESOURCE_TYPE);
        assertThat(request.getRequest().getResource().get(0).getAttribute().get(1).getValue()).isEqualTo("no.nav.abac.attributter.foreldrepenger.fagsak");
    }

    private void validerBasisEnvironmentAttributter(final XacmlRequest request) {
        validerBasisEnvironmentAttributter(request, null, JWT_TOKEN.split("\\.")[1], AbacAttributtNøkkel.ENVIRONMENT_OIDC_TOKEN_BODY);
    }

    private void validerBasisEnvironmentAttributter(final XacmlRequest request, String expectedPepId, String expectedToken, String expectedTokenKey) {
        assertThat(request.getRequest().getEnvironment()).isNotNull();
        assertThat(request.getRequest().getEnvironment().getAttribute()).isNotNull();
        assertThat(request.getRequest().getEnvironment().getAttribute()).hasSize(2);
        assertThat(request.getRequest().getEnvironment().getAttribute().get(0).getAttributeId()).isEqualTo(AbacAttributtNøkkel.ENVIRONMENT_PEP_ID);
        assertThat(request.getRequest().getEnvironment().getAttribute().get(0).getValue()).isEqualTo(expectedPepId);
        assertThat(request.getRequest().getEnvironment().getAttribute().get(1).getAttributeId()).isEqualTo(expectedTokenKey);
        assertThat(request.getRequest().getEnvironment().getAttribute().get(1).getValue()).isEqualTo(expectedToken);
    }

    private void validerBasisActionAttributter(final XacmlRequest request) {
        assertThat(request.getRequest()).isNotNull();
        assertThat(request.getRequest().getAction()).isNotNull();
        assertThat(request.getRequest().getAction().getAttribute()).isNotNull();
        assertThat(request.getRequest().getAction().getAttribute()).hasSize(1);
        assertThat(request.getRequest().getAction().getAttribute().get(0).getAttributeId()).isEqualTo(AbacAttributtNøkkel.ACTION_ACTION_ID);
        assertThat(request.getRequest().getAction().getAttribute().get(0).getValue()).isEqualTo("read");
    }

    private PdpRequest.Builder lagPdpRequestBuilder() {
        return lagPdpRequestBuilder(null);
    }

    private PdpRequest.Builder lagPdpRequestBuilder(IdToken idToken) {
        return PdpRequest.builder()
            .medDomene("foreldre")
            .medActionType(ActionType.READ)
            .medResourceType("no.nav.abac.attributter.foreldrepenger.fagsak")
            .medRequest("/test/request")
            .medUserId("testUser")
            .medIdToken(idToken != null ? idToken : IdToken.withToken(JWT_TOKEN, TokenType.OIDC));
    }

    private static String base64encode(String samlToken) {
        return Base64.getEncoder().encodeToString(samlToken.getBytes(StandardCharsets.UTF_8));
    }
}
