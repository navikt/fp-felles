package no.nav.vedtak.sikkerhet.abac;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
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
import no.nav.vedtak.log.audit.CefFields;
import no.nav.vedtak.log.audit.EventClassId;

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
    
    
    public void loggTilgang(String userId, PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        logg(userId, pdpRequest, attributter, Access.GRANTED);
    }
    

    public void loggDeny(String userId, PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        logg(userId, pdpRequest, attributter, Access.DENIED);
    }
    
    public boolean isEnabled() {
        return auditlogger.isEnabled();
    }
    
    
    private void logg(String userId, PdpRequest pdpRequest, AbacAttributtSamling attributter, Access access) {
        requireNonNull(pdpRequest);
        
        final String abacAction = requireNonNull(pdpRequest.getString(NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID));
        final AuditdataHeader header = createHeader(abacAction, access);
        final Set<CefField> fields = createDefaultAbacFields(userId, pdpRequest, attributter);
        
        List<String> ids = getBerortBrukerId(pdpRequest);
        for (String aktorId : ids) {
            loggTilgangPerBerortAktoer(header, fields, aktorId);
        }
    }

    private void loggTilgangPerBerortAktoer(AuditdataHeader header, Set<CefField> fields, String id) {
        final Set<CefField> fieldsWithBerortBruker = new HashSet<>(fields);
        fieldsWithBerortBruker.add(new CefField(CefFields.BERORT_BRUKER_ID, id));
        final Auditdata auditdata = new Auditdata(header, fieldsWithBerortBruker);
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

    private Set<CefField> createDefaultAbacFields(String userId, PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        final String abacAction = requireNonNull(pdpRequest.getString(NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID));
        final String abacResourceType = requireNonNull(pdpRequest.getString(NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE));
        
        final Set<CefField> fields = new HashSet<>();
        fields.add(new CefField(CefFields.EVENT_TIME, System.currentTimeMillis()));
        fields.add(new CefField(CefFields.REQUEST, attributter.getAction()));
        fields.add(new CefField(CefFields.ABAC_RESOURCE_TYPE, abacResourceType));
        fields.add(new CefField(CefFields.ABAC_ACTION, abacAction));
        
        if (userId != null) {
            fields.add(new CefField(CefFields.USER_ID, userId));
        }
        
        getOneOf(attributter, StandardAbacAttributtType.SAKSNUMMER, StandardAbacAttributtType.FAGSAK_ID).ifPresent(fagsak -> {
            fields.add(new CefField(CefFields.SAKSNUMMER_VERDI, fagsak));
            fields.add(new CefField(CefFields.SAKSNUMMER_LABEL, CefFields.SAKSNUMMER_TEXT));
        });
        
        getOneOf(attributter, StandardAbacAttributtType.BEHANDLING_UUID, StandardAbacAttributtType.BEHANDLING_ID).ifPresent(behandling -> {
            fields.add(new CefField(CefFields.BEHANDLING_VERDI, behandling));
            fields.add(new CefField(CefFields.BEHANDLING_LABEL, CefFields.BEHANDLING_TEXT));            
        });
        
        return Collections.unmodifiableSet(fields);
    }
    
    private List<String> getBerortBrukerId(PdpRequest pdpRequest) {
        /*
         * Arcsight foretrekker FNR fremfor AktørID, men det er uklart hvordan de
         * håndterer blanding (har sendt forespørsel, men ikke fått svar). Velger
         * derfor at AktørID prioriteres (siden alle kallene i k9-sak har denne).
         */
        final List<String> ids = allNonNullValues(pdpRequest, NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE);
        if (!ids.isEmpty()) {
            return ids;
        }    
        return allNonNullValues(pdpRequest, NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR);
    }
    
    private static final Optional<String> getOneOf(AbacAttributtSamling attributter, AbacAttributtType... typer) {
        for (AbacAttributtType key : typer) {
            final Set<Object> values = attributter.getVerdier(key);
            if (!values.isEmpty()) {
                return Optional.of(values.stream().map(v -> v.toString()).collect(Collectors.joining(",")));
            }
        }
        return Optional.empty();
    }

    private static final EventClassId finnEventClassIdFra(String abacAction) {
        switch (abacAction) {
        case "read": return EventClassId.AUDIT_ACCESS;
        case "delete": /* Fall-through */
        case "update": return EventClassId.AUDIT_UPDATE;
        case "create": return EventClassId.AUDIT_CREATE;
        default: throw new IllegalArgumentException("Ukjent abacAction: " + abacAction);
        }
    }

    private static final List<String> allNonNullValues(PdpRequest pdpRequest, String key) {
        return pdpRequest.getListOfString(key).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    /**
     * Standard hos NAV er at tilgang logges som "INFO" og avslag som "WARN". Merk
     * at dette avviker fra CEF-standarden.
     */
    private enum Access {
        /*
         * Det er med vilje ikke brukt andre koder enn "Permit"/"Deny" grunnet
         * at man ved logging mot Arcsight tolker alt annet enn "Permit" som "WARN".
         */
        
        GRANTED("INFO"),
        DENIED("WARN");
        
        private String severity;
        
        private Access(String severity) {
            this.severity = severity;
        }
        
        public String getSeverity() {
            return severity;
        }
    }
}
