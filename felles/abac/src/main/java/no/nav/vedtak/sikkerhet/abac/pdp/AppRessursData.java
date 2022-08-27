package no.nav.vedtak.sikkerhet.abac.pdp;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class AppRessursData {

    private Set<String> aktørIdSet = new LinkedHashSet<>();
    private Set<String> fødselsnumre = new LinkedHashSet<>();
    private Map<RessursDataKey, RessursData> resources = new HashMap<>();

    public Set<String> getAktørIdSet() {
        return aktørIdSet;
    }

    public Set<String> getFødselsnumre() {
        return fødselsnumre;
    }

    public Map<RessursDataKey, RessursData> getResources() {
        return resources;
    }

    public RessursData getResource(RessursDataKey key) {
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

        public Builder leggTilAktørId(String aktørId) {
            pdpRequest.aktørIdSet.add(aktørId);
            return this;
        }

        public Builder leggTilAktørIdSet(Collection<String> aktørId) {
            pdpRequest.aktørIdSet.addAll(aktørId);
            return this;
        }

        public Builder leggTilFødselsnummer(String fnr) {
            pdpRequest.fødselsnumre.add(fnr);
            return this;
        }

        public Builder leggTilFødselsnumre(Collection<String> fnr) {
            pdpRequest.fødselsnumre.addAll(fnr);
            return this;
        }

        public Builder leggTilRessurs(RessursDataKey key, String value) {
            if (value == null) {
                removeKeyIfPresent(key);
                return this;
            }
            pdpRequest.resources.put(key, new RessursData(key, value));
            return this;
        }

        public Builder leggTilRessurs(RessursDataKey key, RessursDataValue value) {
            if (value == null) {
                removeKeyIfPresent(key);
                return this;
            }
            pdpRequest.resources.put(key, new RessursData(key, value.getVerdi()));
            return this;
        }

        public Builder medBehandlingStatus(BehandlingStatus behandlingStatus) {
            return leggTilRessurs(ForeldrepengerDataKeys.BEHANDLING_STATUS, behandlingStatus);
        }

        public Builder medFagsakStatus(FagsakStatus fagsakStatus) {
            return leggTilRessurs(ForeldrepengerDataKeys.FAGSAK_STATUS, fagsakStatus);
        }

        public Builder medOverstyring(Overstyring overstyring) {
            if (!Overstyring.OVERSTYRING.equals(overstyring)) {
                return this;
            }
            return leggTilRessurs(ForeldrepengerDataKeys.AKSJONSPUNKT_OVERSTYRING, overstyring);
        }

        public Builder medAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
            return leggTilRessurs(ForeldrepengerDataKeys.SAKSBEHANDLER, ansvarligSaksbehandler);
        }

        public Builder medAleneomsorg(Boolean aleneomsorg) {
            return leggTilRessurs(ForeldrepengerDataKeys.ALENEOMSORG,
                Optional.ofNullable(aleneomsorg).map(a -> a.toString()).orElse(null));
        }

        public Builder medAnnenpart(String annenpartAktørId) {
            return leggTilRessurs(ForeldrepengerDataKeys.ANNENPART, annenpartAktørId);
        }


        public AppRessursData build() {
            return pdpRequest;
        }

        private void removeKeyIfPresent(RessursDataKey key) {
            if (pdpRequest.resources.get(key) != null) {
                pdpRequest.resources.remove(key);
            }
        }
    }
}
