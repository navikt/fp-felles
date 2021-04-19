package no.nav.foreldrepenger.sikkerhet.abac.auditlog;

import static java.util.Objects.requireNonNull;
import static no.nav.vedtak.log.audit.CefFieldName.ABAC_ACTION;
import static no.nav.vedtak.log.audit.CefFieldName.ABAC_RESOURCE_TYPE;
import static no.nav.vedtak.log.audit.CefFieldName.BERORT_BRUKER_ID;
import static no.nav.vedtak.log.audit.CefFieldName.EVENT_TIME;
import static no.nav.vedtak.log.audit.CefFieldName.REQUEST;
import static no.nav.vedtak.log.audit.CefFieldName.USER_ID;
import static no.nav.vedtak.log.audit.CefFields.forBehandling;
import static no.nav.vedtak.log.audit.CefFields.forSaksnummer;
import static no.nav.vedtak.log.audit.EventClassId.AUDIT_ACCESS;
import static no.nav.vedtak.log.audit.EventClassId.AUDIT_CREATE;
import static no.nav.vedtak.log.audit.EventClassId.AUDIT_UPDATE;
import static no.nav.foreldrepenger.sikkerhet.abac.domene.StandardAbacAttributtType.BEHANDLING_ID;
import static no.nav.foreldrepenger.sikkerhet.abac.domene.StandardAbacAttributtType.BEHANDLING_UUID;
import static no.nav.foreldrepenger.sikkerhet.abac.domene.StandardAbacAttributtType.FAGSAK_ID;
import static no.nav.foreldrepenger.sikkerhet.abac.domene.StandardAbacAttributtType.SAKSNUMMER;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.vedtak.log.audit.Auditdata;
import no.nav.vedtak.log.audit.AuditdataHeader;
import no.nav.vedtak.log.audit.Auditlogger;
import no.nav.vedtak.log.audit.CefField;
import no.nav.vedtak.log.audit.EventClassId;
import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacAttributtType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.BeskyttRessursAttributer;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;


/**
 * Dette loggformatet er avklart med Arcsight. Eventuelle nye felter skal
 * godkjennes av Arcsight. Det er derfor ikke satt opp en løsning for å utvide
 * loggformatet.
 */
@Dependent
public class AbacAuditlogger {

    private final Auditlogger auditlogger;

    @Inject
    public AbacAuditlogger(Auditlogger auditlogger) {
        this.auditlogger = auditlogger;
    }

    public void loggTilgang(PdpRequest pdpRequest, BeskyttRessursAttributer attributter) {
        logg(pdpRequest, attributter, Access.GRANTED);
    }

    public void loggDeny(PdpRequest pdpRequest, BeskyttRessursAttributer attributter) {
        logg(pdpRequest, attributter, Access.DENIED);
    }

    private void logg(PdpRequest pdpRequest, BeskyttRessursAttributer attributter, Access access) {
        requireNonNull(pdpRequest);

        String abacAction = requireNonNull(pdpRequest.getActionId().getEksternKode());
        var header = createHeader(abacAction, access);
        var fields = createDefaultAbacFields(pdpRequest, attributter);

        List<String> ids = getBerortBrukerId(pdpRequest);
        for (String aktorId : ids) {
            loggTilgangPerBerortAktoer(header, fields, aktorId);
        }
    }

    private void loggTilgangPerBerortAktoer(AuditdataHeader header, Set<CefField> fields, String id) {
        var fieldsWithBerortBruker = new HashSet<>(fields);
        fieldsWithBerortBruker.add(new CefField(BERORT_BRUKER_ID, id));
        var auditdata = new Auditdata(header, fieldsWithBerortBruker);
        auditlogger.logg(auditdata);
    }

    private AuditdataHeader createHeader(String abacAction, Access access) {
        return new AuditdataHeader.Builder()
                .medVendor(auditlogger.getDefaultVendor())
                .medProduct(auditlogger.getDefaultProduct())
                .medEventClassId(finnEventClassIdFra(abacAction))
                .medName("ABAC Sporingslogg")
                .medSeverity(access.getSeverity())
                .build();
    }

    private Set<CefField> createDefaultAbacFields(PdpRequest pdpRequest, BeskyttRessursAttributer attributter) {
        String abacAction = requireNonNull(pdpRequest.getActionId().getEksternKode());
        String abacResourceType = requireNonNull(pdpRequest.getResourceType());

        Set<CefField> fields = new HashSet<>();
        fields.add(new CefField(EVENT_TIME, System.currentTimeMillis()));
        fields.add(new CefField(ABAC_RESOURCE_TYPE, abacResourceType));
        fields.add(new CefField(ABAC_ACTION, abacAction));
        fields.add(new CefField(REQUEST, pdpRequest.getRequest()));

        if (pdpRequest.getUserId() != null) {
            fields.add(new CefField(USER_ID, pdpRequest.getUserId()));
        }

        getOneOf(attributter, SAKSNUMMER, FAGSAK_ID).ifPresent(fagsak -> {
            fields.addAll(forSaksnummer(fagsak));
        });

        getOneOf(attributter, BEHANDLING_UUID, BEHANDLING_ID).ifPresent(behandling -> {
            fields.addAll(forBehandling(behandling));
        });

        return Set.copyOf(fields);
    }

    private List<String> getBerortBrukerId(PdpRequest pdpRequest) {
        /*
         * Arcsight foretrekker FNR fremfor AktørID, men det er uklart hvordan de
         * håndterer blanding (har sendt forespørsel, men ikke fått svar). Velger derfor
         * at AktørID prioriteres.
         */
        final List<String> ids = allNonNullValues(pdpRequest.getAktørIder());
        if (!ids.isEmpty()) {
            return ids;
        }
        return allNonNullValues(pdpRequest.getPersonnummere());
    }

    private static Optional<String> getOneOf(BeskyttRessursAttributer attributter, AbacAttributtType... typer) {
        for (AbacAttributtType key : typer) {
            final Set<Object> values = attributter.getVerdier(key);
            if (!values.isEmpty()) {
                return Optional.of(values.stream().map(Object::toString).collect(Collectors.joining(",")));
            }
        }
        return Optional.empty();
    }

    private static EventClassId finnEventClassIdFra(String abacAction) {
        switch (abacAction) {
            case "read": return AUDIT_ACCESS;
            case "delete": case "update": return AUDIT_UPDATE;
            case "create": return AUDIT_CREATE;
            default: throw new IllegalArgumentException("Ukjent abacAction: " + abacAction);
        }
    }

    private static List<String> allNonNullValues(Set<String> identer) {
        return identer.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Standard hos NAV er at tilgang logges som "INFO" og avslag som "WARN". Merk
     * at dette avviker fra CEF-standarden.
     */
    private enum Access {
        /*
         * Det er med vilje ikke brukt andre koder enn "Permit"/"Deny" grunnet at man
         * ved logging mot Arcsight tolker alt annet enn "Permit" som "WARN".
         */

        GRANTED("INFO"),
        DENIED("WARN");

        private final String severity;

        private Access(String severity) {
            this.severity = severity;
        }

        public String getSeverity() {
            return severity;
        }
    }
}
