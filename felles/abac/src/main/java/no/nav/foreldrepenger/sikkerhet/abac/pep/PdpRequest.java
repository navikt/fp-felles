package no.nav.foreldrepenger.sikkerhet.abac.pep;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacBehandlingStatus;
import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacFagsakStatus;
import no.nav.foreldrepenger.sikkerhet.abac.domene.ActionType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdSubject;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdToken;

public class PdpRequest {

    private String userId;
    private IdToken idToken;
    private ActionType actionId;
    private String resourceType;
    private String request;
    private String domene;
    private String pepId;
    private IdSubject idSubject;
    private Set<String> aktørIder = Collections.emptySet();
    private Set<String> personnummere = Collections.emptySet();
    private Set<String> aksjonspunkter = Collections.emptySet();
    private AbacBehandlingStatus behandlingStatus;
    private AbacFagsakStatus fagsakStatus;
    private String ansvarligSaksbenandler;
    private Boolean aleneomsorg;
    private String annenPartAktørId;

    public static Builder builder() {
        return new Builder();
    }

    private PdpRequest() {}

    private PdpRequest(final String userId,
                       final IdToken idToken,
                       final ActionType actionId,
                       final String resourceType,
                       final String request,
                       final String domene,
                       final String pepId) {
        this.userId = userId;
        this.idToken = idToken;
        this.actionId = actionId;
        this.resourceType = resourceType;
        this.request = request;
        this.domene = domene;
        this.pepId = pepId;
    }

    public String getUserId() {
        return userId;
    }

    public IdToken getIdToken() {
        return idToken;
    }

    public ActionType getActionId() {
        return actionId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getRequest() {
        return request;
    }

    public Optional<String> getDomene() {
        return Optional.ofNullable(domene);
    }

    public Optional<String> getPepId() {
        return Optional.ofNullable(pepId);
    }

    public Optional<IdSubject> getIdSubject() {
        return Optional.ofNullable(idSubject);
    }

    public void setIdSubject(final IdSubject idSubject) {
        this.idSubject = idSubject;
    }

    public Set<String> getAktørIder() {
        return aktørIder;
    }

    public void setAktørIder(final Set<String> aktørIder) {
        this.aktørIder = aktørIder;
    }

    public Set<String> getPersonnummere() {
        return personnummere;
    }

    public void setPersonnummere(final Set<String> personnummere) {
        this.personnummere = personnummere;
    }

    public Set<String> getAksjonspunkter() {
        return aksjonspunkter;
    }

    public void setAksjonspunkter(final Set<String> aksjonspunkter) {
        this.aksjonspunkter = aksjonspunkter;
    }

    public Optional<AbacBehandlingStatus> getBehandlingStatus() {
        return Optional.ofNullable(behandlingStatus);
    }

    public void setBehandlingStatus(final AbacBehandlingStatus behandlingStatus) {
        this.behandlingStatus = behandlingStatus;
    }

    public Optional<AbacFagsakStatus> getFagsakStatus() {
        return Optional.ofNullable(fagsakStatus);
    }

    public void setFagsakStatus(final AbacFagsakStatus fagsakStatus) {
        this.fagsakStatus = fagsakStatus;
    }

    public Optional<String> getAnsvarligSaksbenandler() {
        return Optional.ofNullable(ansvarligSaksbenandler);
    }

    public void setAnsvarligSaksbenandler(final String ansvarligSaksbenandler) {
        this.ansvarligSaksbenandler = ansvarligSaksbenandler;
    }

    public Optional<Boolean> getAleneomsorg() {
        return Optional.ofNullable(aleneomsorg);
    }

    public void setAleneomsorg(final Boolean aleneomsorg) {
        this.aleneomsorg = aleneomsorg;
    }

    public Optional<String> getAnnenPartAktørId() {
        return Optional.ofNullable(annenPartAktørId);
    }

    public void setAnnenPartAktørId(final String annenPartAktørId) {
        this.annenPartAktørId = annenPartAktørId;
    }

    @Override
    public String toString() {
        return "PdpRequest{" +
            "userId='MASKERT'" +
            ", idToken=" + idToken +
            ", actionId=" + actionId +
            ", resourceType='" + resourceType + '\'' +
            ", request='" + request + '\'' +
            ", domene='" + domene + '\'' +
            ", pepId='" + pepId + '\'' +
            ", idSubject=" + idSubject +
            ", aktørIder=" + aktørIder +
            ", fnre=" + personnummere +
            ", aksjonspunkter=" + aksjonspunkter +
            ", behandlingStatus=" + behandlingStatus +
            ", fagsakStatus=" + fagsakStatus +
            ", ansvarligSaksbenandler='" + ansvarligSaksbenandler + '\'' +
            ", aleneomsorg=" + aleneomsorg +
            ", annenPartAktørId='MASKERT'" +
            '}';
    }

    public static class Builder {
        private final PdpRequest pdpRequest;

        public Builder() {
            pdpRequest = new PdpRequest();
        }

        public Builder medUserId(String userId) {
            pdpRequest.userId = userId;
            return this;
        }

        public Builder medIdToken(IdToken idToken) {
            pdpRequest.idToken = idToken;
            return this;
        }

        public Builder medActionType(ActionType actionType) {
            pdpRequest.actionId = actionType;
            return this;
        }

        public Builder medResourceType(String resourceType) {
            pdpRequest.resourceType = resourceType;
            return this;
        }

        public Builder medRequest(String request) {
            pdpRequest.request = request;
            return this;
        }

        public Builder medDomene(String domene) {
            pdpRequest.domene = domene;
            return this;
        }

        public Builder medPepId(String pepId) {
            pdpRequest.pepId = pepId;
            return this;
        }

        public PdpRequest build() {
            validateBeforeBuild();
            return pdpRequest;
        }

        private void validateBeforeBuild() {
            Objects.requireNonNull(pdpRequest.userId, "userId");
            Objects.requireNonNull(pdpRequest.idToken, "idToken");
            Objects.requireNonNull(pdpRequest.actionId, "actionId");
            Objects.requireNonNull(pdpRequest.resourceType, "resourceType");
            Objects.requireNonNull(pdpRequest.request, "request");
        }
    }
}
