package no.nav.vedtak.sikkerhet.abac.internal;

import java.util.Objects;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.Token;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ServiceType;


public class BeskyttetRessursAttributter {

    private String userId;
    private ServiceType serviceType;
    private ActionType actionType;
    private String resourceType;
    private AvailabilityType availabilityType;
    private Token token;
    private String pepId;
    private String servicePath;
    private AbacDataAttributter dataAttributter;

    public static Builder builder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public AvailabilityType getAvailabilityType() {
        return availabilityType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Token getToken() {
        return token;
    }

    public String getPepId() {
        return pepId;
    }

    public String getServicePath() {
        return servicePath;
    }

    public AbacDataAttributter getDataAttributter() {
        return dataAttributter;
    }

    @Override
    public String toString() {
        return "BeskyttetRessursAttributter{" + "userId=MASKERT" + ", serviceType=" + serviceType + ", actionType=" + actionType + ", resourceType="
            + resourceType + ", token=" + token + ", pepId=" + pepId + ", servicePath=" + servicePath + '}';
    }

    public static class Builder {
        private final BeskyttetRessursAttributter pdpRequest;

        public Builder() {
            pdpRequest = new BeskyttetRessursAttributter();
        }

        public Builder medUserId(String userId) {
            pdpRequest.userId = userId;
            return this;
        }

        public Builder medToken(Token token) {
            pdpRequest.serviceType = Token.TokenType.SAML.equals(token.getTokenType()) ? ServiceType.WEBSERVICE : ServiceType.REST;
            pdpRequest.token = token;
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

        public Builder medServiceType(ServiceType serviceType) {
            pdpRequest.serviceType = serviceType;
            return this;
        }

        public Builder medResourceType(ResourceType resourceType) {
            pdpRequest.resourceType = resourceType.getResourceTypeAttribute();
            return this;
        }

        public Builder medResourceType(String resourceType) {
            pdpRequest.resourceType = resourceType;
            return this;
        }

        public Builder medPepId(String pepId) {
            pdpRequest.pepId = pepId;
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
            Objects.requireNonNull(pdpRequest.userId, "userId");
            Objects.requireNonNull(pdpRequest.token, "idToken");
            Objects.requireNonNull(pdpRequest.actionType, "actionType");
            Objects.requireNonNull(pdpRequest.resourceType, "resourceType");
            Objects.requireNonNull(pdpRequest.pepId, "pepId");
            Objects.requireNonNull(pdpRequest.servicePath, "servicePath");
            Objects.requireNonNull(pdpRequest.dataAttributter, "dataAttributter");
        }
    }
}
