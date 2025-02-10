package no.nav.vedtak.sikkerhet.abac.internal;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;


public class BeskyttetRessursAttributter {

    private String brukerId;
    private UUID brukerOid;
    private IdentType identType;
    private final Set<AnsattGruppe> ansattGrupper = new LinkedHashSet<>();
    private ActionType actionType;
    private ResourceType resourceType;
    private AvailabilityType availabilityType;
    private String servicePath;
    private boolean sporingslogg = true;
    private AbacDataAttributter dataAttributter;

    public static Builder builder() {
        return new Builder();
    }

    public String getBrukerId() {
        return brukerId;
    }

    public IdentType getIdentType() {
        return identType;
    }

    public UUID getBrukerOid() {
        return brukerOid;
    }

    public Set<AnsattGruppe> getAnsattGrupper() {
        return ansattGrupper;
    }

    public AvailabilityType getAvailabilityType() {
        return availabilityType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public boolean isSporingslogg() {
        return sporingslogg;
    }

    public String getServicePath() {
        return servicePath;
    }

    public AbacDataAttributter getDataAttributter() {
        return dataAttributter;
    }

    @Override
    public String toString() {
        return "BeskyttetRessursAttributter{" + "userId=MASKERT" + ", actionType=" + actionType + ", resourceType="
            + resourceType + ", servicePath=" + servicePath + '}';
    }

    public static class Builder {
        private final BeskyttetRessursAttributter pdpRequest;

        public Builder() {
            pdpRequest = new BeskyttetRessursAttributter();
        }

        public Builder medBrukerId(String brukerId) {
            pdpRequest.brukerId = brukerId;
            return this;
        }

        public Builder medBrukerOid(UUID oid) {
            pdpRequest.brukerOid = oid;
            return this;
        }

        public Builder medIdentType(IdentType identType) {
            pdpRequest.identType = identType;
            return this;
        }

        public Builder medAnsattGrupper(Set<AnsattGruppe> ansattGrupper) {
            pdpRequest.ansattGrupper.addAll(ansattGrupper);
            return this;
        }

        public Builder medActionType(ActionType actionType) {
            pdpRequest.actionType = actionType;
            return this;
        }

        public Builder medAvailabilityType(AvailabilityType availabilityType) {
            pdpRequest.availabilityType = availabilityType;
            return this;
        }

        public Builder medResourceType(ResourceType resourceType) {
            pdpRequest.resourceType = resourceType;
            return this;
        }

        public Builder medSporingslogg(boolean sporingslogg) {
            pdpRequest.sporingslogg = sporingslogg;
            return this;
        }

        public Builder medServicePath(String servicePath) {
            pdpRequest.servicePath = servicePath;
            return this;
        }

        public Builder medDataAttributter(AbacDataAttributter dataAttributter) {
            pdpRequest.dataAttributter = dataAttributter;
            return this;
        }

        public BeskyttetRessursAttributter build() {
            validateBeforeBuild();
            return pdpRequest;
        }

        private void validateBeforeBuild() {
            Objects.requireNonNull(pdpRequest.brukerId, "userId");
            Objects.requireNonNull(pdpRequest.actionType, "actionType");
            Objects.requireNonNull(pdpRequest.resourceType, "resourceType");
            Objects.requireNonNull(pdpRequest.identType, "identType");
            Objects.requireNonNull(pdpRequest.servicePath, "servicePath");
            Objects.requireNonNull(pdpRequest.dataAttributter, "dataAttributter");
        }
    }
}
