package no.nav.vedtak.sikkerhet.abac.pdp;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipOverstyring;


public class AppRessursData {

    private UUID behandling;
    private String saksnummer;
    private final Set<String> identer = new LinkedHashSet<>();
    private final Map<ForeldrepengerDataKeys, RessursData> resources = new EnumMap<>(ForeldrepengerDataKeys.class);

    public String getSaksnummer() {
        return saksnummer;
    }

    public UUID getBehandling() {
        return behandling;
    }

    public Set<String> getIdenter() {
        return identer;
    }

    public RessursData getResource(ForeldrepengerDataKeys key) {
        return resources.get(key);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "AppRessursData{}";
    }

    public static class Builder {
        private final AppRessursData pdpRequest;

        public Builder() {
            pdpRequest = new AppRessursData();
        }

        public Builder medSaksnummer(String saksnummer) {
            if (saksnummer == null || pdpRequest.saksnummer != null) {
                throw new IllegalArgumentException("Utviklerfeil: saksnummer er null eller allerede satt");
            }
            pdpRequest.saksnummer = saksnummer;
            return this;
        }

        public Builder medBehandling(UUID behandling) {
            if (behandling == null || pdpRequest.behandling != null) {
                throw new IllegalArgumentException("Utviklerfeil: behandling er null eller allerede satt");
            }
            pdpRequest.behandling = behandling;
            return this;
        }

        public Builder leggTilIdent(String ident) {
            pdpRequest.identer.add(ident);
            return this;
        }

        public Builder leggTilIdenter(Collection<String> identer) {
            pdpRequest.identer.addAll(identer);
            return this;
        }

        public Builder medBehandlingStatus(PipBehandlingStatus behandlingStatus) {
            return leggTilRessurs(ForeldrepengerDataKeys.BEHANDLING_STATUS, behandlingStatus);
        }

        public Builder medFagsakStatus(PipFagsakStatus fagsakStatus) {
            return leggTilRessurs(ForeldrepengerDataKeys.FAGSAK_STATUS, fagsakStatus);
        }

        public Builder medOverstyring(PipOverstyring overstyring) {
            if (!PipOverstyring.OVERSTYRING.equals(overstyring)) {
                return this;
            }
            return leggTilRessurs(ForeldrepengerDataKeys.AKSJONSPUNKT_OVERSTYRING, overstyring);
        }

        public Builder medAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
            return leggTilRessurs(ForeldrepengerDataKeys.SAKSBEHANDLER, ansvarligSaksbehandler);
        }

        public Builder medAvdelingEnhet(String enhet) {
            return leggTilRessurs(ForeldrepengerDataKeys.AVDELING_ENHET, enhet);
        }

        public Builder medAleneomsorg(Boolean aleneomsorg) {
            return leggTilRessurs(ForeldrepengerDataKeys.ALENEOMSORG, Optional.ofNullable(aleneomsorg).map(Object::toString).orElse(null));
        }

        public Builder medAnnenpart(String annenpartIdent) {
            return leggTilRessurs(ForeldrepengerDataKeys.ANNENPART, annenpartIdent);
        }

        public AppRessursData build() {
            return pdpRequest;
        }

        private Builder leggTilRessurs(ForeldrepengerDataKeys key, String value) {
            if (value == null) {
                removeKeyIfPresent(key);
                return this;
            }
            pdpRequest.resources.put(key, new RessursData(key, value));
            return this;
        }

        private Builder leggTilRessurs(ForeldrepengerDataKeys key, RessursDataValue value) {
            if (value == null) {
                removeKeyIfPresent(key);
                return this;
            }
            pdpRequest.resources.put(key, new RessursData(key, value.getVerdi()));
            return this;
        }

        private void removeKeyIfPresent(ForeldrepengerDataKeys key) {
            if (pdpRequest.resources.get(key) != null) {
                pdpRequest.resources.remove(key);
            }
        }
    }
}
