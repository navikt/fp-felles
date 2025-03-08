package no.nav.vedtak.sikkerhet.abac;

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
import static no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType.BEHANDLING_ID;
import static no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType.BEHANDLING_UUID;
import static no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType.FAGSAK_ID;
import static no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType.SAKSNUMMER;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.log.audit.Auditdata;
import no.nav.vedtak.log.audit.AuditdataHeader;
import no.nav.vedtak.log.audit.Auditlogger;
import no.nav.vedtak.log.audit.CefField;
import no.nav.vedtak.log.audit.EventClassId;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;

/**
 * Dette loggformatet er avklart med Arcsight. Eventuelle nye felter skal
 * godkjennes av Arcsight. Det er derfor ikke satt opp en løsning for å utvide
 * loggformatet.
 */
@Dependent
public class AbacAuditlogger {

    private static final Logger LOG = LoggerFactory.getLogger(AbacAuditlogger.class);

    private final Auditlogger auditlogger;

    @Inject
    public AbacAuditlogger(Auditlogger auditlogger) {
        this.auditlogger = auditlogger;
    }

    public void loggUtfall(AbacResultat utfall, BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (auditlogger.auditLogDisabled()) {
            return;
        }
        if (IdentType.Systemressurs.equals(beskyttetRessursAttributter.getIdentType())) {
            // Skal ikke auditlogge systemkall
            if (!utfall.fikkTilgang()) {
                LOG.info("ABAC AVSLAG SYSTEMBRUKER {} tjeneste {}", beskyttetRessursAttributter.getBrukerId(), beskyttetRessursAttributter.getServicePath());
            }
        } else if (beskyttetRessursAttributter.isSporingslogg()) {
            try {
                logg(beskyttetRessursAttributter, appRessursData, utfall.fikkTilgang() ? Access.GRANTED : Access.DENIED);
            } catch (Exception e) {
                LOG.warn("ABAC AUDITLOG FAILURE ident {} tjeneste {}", beskyttetRessursAttributter.getIdentType(), beskyttetRessursAttributter.getServicePath(), e);
            }

        }
    }

    private void logg(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData, Access access) {
        requireNonNull(beskyttetRessursAttributter);
        requireNonNull(beskyttetRessursAttributter.getDataAttributter());

        var header = createHeader(beskyttetRessursAttributter.getActionType(), access);
        var fields = createDefaultAbacFields(beskyttetRessursAttributter);

        List<String> ids = getBerortBrukerId(appRessursData); // Beholder appRessursData for K9-tilbake
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

    private AuditdataHeader createHeader(ActionType abacAction, Access access) {
        return new AuditdataHeader.Builder().medVendor(auditlogger.getDefaultVendor())
            .medProduct(auditlogger.getDefaultProduct())
            .medEventClassId(finnEventClassIdFra(abacAction))
            .medName("ABAC Sporingslogg")
            .medSeverity(access.getSeverity())
            .build();
    }

    protected Set<CefField> createDefaultAbacFields(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        var abacAction = requireNonNull(mapActionType(beskyttetRessursAttributter.getActionType()));
        var abacResourceType = requireNonNull(mapResourceType(beskyttetRessursAttributter.getResourceType()));

        Set<CefField> fields = new HashSet<>();
        fields.add(new CefField(EVENT_TIME, System.currentTimeMillis()));
        fields.add(new CefField(REQUEST, beskyttetRessursAttributter.getServicePath()));
        fields.add(new CefField(ABAC_RESOURCE_TYPE, abacResourceType));
        fields.add(new CefField(ABAC_ACTION, abacAction));

        if (beskyttetRessursAttributter.getBrukerId() != null) {
            fields.add(new CefField(USER_ID, beskyttetRessursAttributter.getBrukerId()));
        }

        getOneOfNew(beskyttetRessursAttributter.getDataAttributter(), SAKSNUMMER, FAGSAK_ID).ifPresent(fagsak -> fields.addAll(forSaksnummer(fagsak)));

        getOneOfNew(beskyttetRessursAttributter.getDataAttributter(), BEHANDLING_UUID, BEHANDLING_ID).ifPresent(behandling -> fields.addAll(forBehandling(behandling)));

        return Set.copyOf(fields);
    }

    private List<String> getBerortBrukerId(AppRessursData appRessursData) {
        if (appRessursData.getAuditIdent() != null) {
            return List.of(appRessursData.getAuditIdent());
        }
        /*
         * Arcsight foretrekker FNR fremfor AktørID, men det er uklart hvordan de
         * håndterer blanding (har sendt forespørsel, men ikke fått svar). Velger derfor
         * at AktørID prioriteres (siden alle kallene i k9-sak har denne).
         */
        final var ids = appRessursData.getAktørIdSet().stream().filter(Objects::nonNull).toList();
        if (!ids.isEmpty()) {
            return ids;
        }
        return appRessursData.getFødselsnumre().stream().filter(Objects::nonNull).toList();
    }

    private static Optional<String> getOneOfNew(AbacDataAttributter attributter, AbacAttributtType... typer) {
        for (var key : typer) {
            final var values = attributter.getVerdier(key);
            if (!values.isEmpty()) {
                return Optional.of(values.stream().map(Object::toString).collect(Collectors.joining(",")));
            }
        }
        return Optional.empty();
    }

    private static EventClassId finnEventClassIdFra(ActionType abacAction) {
        return switch (abacAction) {
            case READ -> AUDIT_ACCESS; /* Fall-through */
            case DELETE, UPDATE -> AUDIT_UPDATE;
            case CREATE -> AUDIT_CREATE;
            default -> throw new IllegalArgumentException("Ukjent abacAction: " + abacAction);
        };
    }

    private static String mapActionType(ActionType actionType) {
        return actionType == ActionType.DUMMY ? null : actionType.name().toLowerCase();
    }

    // TODO: vurder å forkorte vekk abac.attributter.
    private static String mapResourceType(ResourceType resourceType) {
        return switch (resourceType) {
            case APPLIKASJON -> "no.nav.abac.attributter.foreldrepenger";
            case DRIFT -> "no.nav.abac.attributter.foreldrepenger.drift";
            case FAGSAK -> "no.nav.abac.attributter.foreldrepenger.fagsak";
            case VENTEFRIST -> "no.nav.abac.attributter.foreldrepenger.fagsak.ventefrist";
            case OPPGAVESTYRING_AVDELINGENHET -> "no.nav.abac.attributter.foreldrepenger.oppgavestyring.avdelingsenhet";
            case OPPGAVESTYRING -> "no.nav.abac.attributter.foreldrepenger.oppgavestyring";
            case UTTAKSPLAN -> "no.nav.abac.attributter.foreldrepenger.uttaksplan";
            case PIP, DUMMY -> throw new IllegalArgumentException("Ulovlig resourceType: " + resourceType);
        };
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

        Access(String severity) {
            this.severity = severity;
        }

        public String getSeverity() {
            return severity;
        }
    }
}
