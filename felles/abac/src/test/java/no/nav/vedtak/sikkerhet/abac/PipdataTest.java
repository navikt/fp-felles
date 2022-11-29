package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.abac.pipdata.AbacPipDto;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipAktørId;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PipdataTest {

    @Test
    void roundtrip_pip() {
        var pip = new AbacPipDto(Set.of(new PipAktørId("0000000000000")), PipFagsakStatus.UNDER_BEHANDLING, PipBehandlingStatus.UTREDES);
        var json = DefaultJsonMapper.toJson(pip);
        var roundtrip = DefaultJsonMapper.fromJson(json, AbacPipDto.class);
        assertThat(roundtrip).isEqualTo(pip);
    }

    @Test
    void roundtrip_compatible1() {
        var pip = new PseudoPip(Set.of("0000000000000", "2222222222222"), PipFagsakStatus.UNDER_BEHANDLING.name(), PipBehandlingStatus.UTREDES.name());
        var json = DefaultJsonMapper.toJson(pip);
        var roundtrip = DefaultJsonMapper.fromJson(json, AbacPipDto.class);
        assertThat(roundtrip.aktørIder().stream().map(PipAktørId::getVerdi).collect(Collectors.toSet())).containsAll(pip.aktørIder());
        assertThat(roundtrip.fagsakStatus().name()).isEqualTo(pip.fagsakStatus());
        assertThat(roundtrip.behandlingStatus().name()).isEqualTo(pip.behandlingStatus());
    }

    @Test
    void roundtrip_compatible2() {
        var pip = new AbacPipDto(Set.of(new PipAktørId("0000000000000"), new PipAktørId("2222222222222")), PipFagsakStatus.UNDER_BEHANDLING, PipBehandlingStatus.UTREDES);
        var json = DefaultJsonMapper.toJson(pip);
        var roundtrip = DefaultJsonMapper.fromJson(json, PseudoPip.class);
        assertThat(pip.aktørIder().stream().map(PipAktørId::getVerdi).collect(Collectors.toSet())).containsAll(roundtrip.aktørIder());
        assertThat(pip.fagsakStatus().name()).isEqualTo(roundtrip.fagsakStatus());
        assertThat(pip.behandlingStatus().name()).isEqualTo(roundtrip.behandlingStatus());
    }

    @Test
    void fra_kilde_sak() {
        var json = """
            {"aktørIder":["0000000000000","2222222222222"],"fagsakStatus":"UNDER_BEHANDLING","behandlingStatus":"UTREDES"}
            """;
        var roundtrip = DefaultJsonMapper.fromJson(json, AbacPipDto.class);
        assertThat(roundtrip.aktørIder().stream().map(PipAktørId::getVerdi).collect(Collectors.toSet())).containsAll(Set.of("0000000000000","2222222222222"));
        assertThat(roundtrip.fagsakStatus()).isEqualTo(PipFagsakStatus.UNDER_BEHANDLING);
        assertThat(roundtrip.behandlingStatus()).isEqualTo(PipBehandlingStatus.UTREDES);
    }

    private static record PseudoPip(Set<String> aktørIder, String fagsakStatus, String behandlingStatus) {}

}
