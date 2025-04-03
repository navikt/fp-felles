package no.nav.vedtak.sikkerhet.abac.pdp;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.vedtak.sikkerhet.abac.pipdata.PipAktørId;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipOverstyring;


public class AppRessursData {

    @Deprecated(forRemoval = true)
    private String auditAktørId;

    private String auditIdent;

    private String saksnummer;
    private final Set<String> aktørIdSet = new LinkedHashSet<>();
    private final Set<String> fødselsnumre = new LinkedHashSet<>();
    private final Map<ForeldrepengerDataKeys, RessursData> resources = new HashMap<>();

    public String getAuditIdent() {
        return Optional.ofNullable(auditIdent).orElse(auditAktørId);
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public Set<String> getAktørIdSet() {
        return aktørIdSet;
    }

    public Set<String> getFødselsnumre() {
        return fødselsnumre;
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

        @Deprecated(forRemoval = true)
        public Builder medAuditAktørId(String aktørId) {
            pdpRequest.auditIdent = aktørId;
            pdpRequest.auditAktørId = aktørId;
            return this;
        }

        public Builder medAuditIdent(String ident) {
            pdpRequest.auditIdent = ident;
            return this;
        }

        public Builder medSaksnummer(String saksnummer) {
            if (saksnummer == null || pdpRequest.saksnummer != null) {
                throw new IllegalArgumentException("Utviklerfeil: saksnummer er null eller allerede satt");
            }
            pdpRequest.saksnummer = saksnummer;
            return this;
        }

        public Builder leggTilAktørId(String aktørId) {
            pdpRequest.aktørIdSet.add(aktørId);
            return this;
        }

        public Builder leggTilAktørIdSet(Collection<String> aktørId) {
            pdpRequest.aktørIdSet.addAll(aktørId);
            return this;
        }

        public Builder leggTilAbacAktørIdSet(Collection<PipAktørId> aktørId) {
            pdpRequest.aktørIdSet.addAll(aktørId.stream().map(PipAktørId::getVerdi).collect(Collectors.toSet()));
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

        private void removeKeyIfPresent(ForeldrepengerDataKeys key) {
            if (pdpRequest.resources.get(key) != null) {
                pdpRequest.resources.remove(key);
            }
        }
    }
}
