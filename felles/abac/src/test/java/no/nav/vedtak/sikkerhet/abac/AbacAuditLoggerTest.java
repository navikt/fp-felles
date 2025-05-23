package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.ws.rs.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.log.audit.Auditdata;
import no.nav.vedtak.log.audit.Auditlogger;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.ActionUthenter;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.tilgang.TilgangResultat;

@ExtendWith(MockitoExtension.class)
class AbacAuditLoggerTest {

    private final AktørDto aktør1 = new AktørDto("00000000000");
    private final BehandlingIdDto behandlingIdDto = new BehandlingIdDto(UUID.randomUUID());

    private final ArgumentCaptor<Auditdata> auditdataCaptor = ArgumentCaptor.forClass(Auditdata.class);

    @Test
    void auditlogg_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit() throws Exception {
        final var auditlogger = mockAuditLogger();
        final var abacAuditlogger = new AbacAuditlogger(auditlogger);

        var method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        var appRessursData = AppRessursData.builder().build();
        var beskyttetRessursAttributter = getBeskyttetRessursAttributter(method,
            BeskyttetRessursInterceptor.finnAbacDataAttributter(method, new Object[]{aktør1}));
        var vurdering = new Tilgangsvurdering(TilgangResultat.GODKJENT, "", Set.of(), aktør1.aktørId());

        abacAuditlogger.loggUtfall(vurdering, beskyttetRessursAttributter, appRessursData);
        assertGotPattern(auditlogger,
            "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|INFO|act=create duid=00000000000 end=__NUMBERS__ request=/foo/aktoer_in requestContext=no.nav.abac.attributter.foreldrepenger.fagsak suid=A000000");
    }

    @Test
    void auditlogg_skal_også_logge_input_parametre_til_sporingslogg_ved_permit() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);

        var method = RestClass.class.getMethod("behandlingIdIn", BehandlingIdDto.class);
        var appRessursData = AppRessursData.builder().build();
        var beskyttetRessursAttributter = getBeskyttetRessursAttributter(method,
            BeskyttetRessursInterceptor.finnAbacDataAttributter(method, new Object[]{behandlingIdDto}));
        var vurdering = new Tilgangsvurdering(TilgangResultat.GODKJENT, "", Set.of(), aktør1.aktørId());

        abacAuditlogger.loggUtfall(vurdering, beskyttetRessursAttributter, appRessursData);

        assertGotPattern(auditlogger,
            "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|INFO|act=create duid=00000000000 end=__NUMBERS__ flexString2=__HEX__ flexString2Label=Behandling request=/foo/behandling_id_in requestContext=no.nav.abac.attributter.foreldrepenger.fagsak suid=A000000");
    }

    @Test
    void auditlog_skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering() throws Exception {
        final Auditlogger auditlogger = mock(Auditlogger.class);
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);

        var method = RestClass.class.getMethod("utenSporingslogg", BehandlingIdDto.class);
        var appRessursData = AppRessursData.builder().build();
        var beskyttetRessursAttributter = getBeskyttetRessursAttributter(method,
            BeskyttetRessursInterceptor.finnAbacDataAttributter(method, new Object[]{behandlingIdDto}));
        var vurdering = new Tilgangsvurdering(TilgangResultat.GODKJENT, "", Set.of(), aktør1.aktørId());

        abacAuditlogger.loggUtfall(vurdering, beskyttetRessursAttributter, appRessursData);

        verify(auditlogger, never()).logg(Mockito.any());
    }

    @Test
    void auditlog_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);

        var method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        var appRessursData = AppRessursData.builder().build();
        var beskyttetRessursAttributter = getBeskyttetRessursAttributter(method,
            BeskyttetRessursInterceptor.finnAbacDataAttributter(method, new Object[]{aktør1}));
        var vurdering = new Tilgangsvurdering(TilgangResultat.AVSLÅTT_KODE_6, "", Set.of(), aktør1.aktørId());

        abacAuditlogger.loggUtfall(vurdering, beskyttetRessursAttributter, appRessursData);

        assertGotPattern(auditlogger,
            "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|WARN|act=create duid=00000000000 end=__NUMBERS__ request=/foo/aktoer_in requestContext=no.nav.abac.attributter.foreldrepenger.fagsak suid=A000000");
    }


    private void assertGotPattern(final Auditlogger auditlogger, String expected) {
        verify(auditlogger).logg(auditdataCaptor.capture());
        final String actual = auditdataCaptor.getValue().toString();
        assertThat(actual).matches(toAuditdataPattern(expected));
    }

    private static Auditlogger mockAuditLogger() {
        final Auditlogger auditlogger = mock(Auditlogger.class);
        when(auditlogger.auditLogDisabled()).thenReturn(Boolean.FALSE);
        when(auditlogger.getDefaultVendor()).thenReturn("felles");
        when(auditlogger.getDefaultProduct()).thenReturn("felles-test");
        return auditlogger;
    }

    private BeskyttetRessursAttributter getBeskyttetRessursAttributter(Method method, AbacDataAttributter dataAttributter) {
        var beskyttetRessurs = method.getAnnotation(BeskyttetRessurs.class);

        return BeskyttetRessursAttributter.builder()
            .medBrukerId("A000000")
            .medBrukerOid(UUID.randomUUID())
            .medIdentType(IdentType.InternBruker)
            .medAnsattGrupper(Set.of())
            .medActionType(beskyttetRessurs.actionType())
            .medAvailabilityType(beskyttetRessurs.availabilityType())
            .medResourceType(beskyttetRessurs.resourceType())
            .medSporingslogg(beskyttetRessurs.sporingslogg())
            .medServicePath(ActionUthenter.action(RestClass.class, method))
            .medDataAttributter(dataAttributter)
            .build();

    }

    private static String toAuditdataPattern(String s) {
        return Pattern.quote(s)
            .replaceAll("__NUMBERS__", unquoteInReplacement("[0-9]*"))
            .replaceAll("__HEX__", unquoteInReplacement("[0-9a-f-]*"));
    }

    private static String unquoteInReplacement(String s) {
        return "\\\\E" + s + "\\\\Q";
    }

    @Path("foo")
    static class RestClass {

        @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
        @Path("aktoer_in")
        public void aktoerIn(@SuppressWarnings("unused") AktørDto param) {
            // Test
        }

        @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK, sporingslogg = true)
        @Path("behandling_id_in")
        public void behandlingIdIn(@SuppressWarnings("unused") BehandlingIdDto param) {
            // Test
        }

        @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK, sporingslogg = false)
        @Path("uten_sporingslogg")
        public void utenSporingslogg(@SuppressWarnings("unused") BehandlingIdDto param) {
            // Test
        }

    }

    private record AktørDto(String aktørId) implements AbacDto {

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, aktørId);
        }
    }

    private record BehandlingIdDto(UUID id) implements AbacDto {

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, id);
        }
    }

}
