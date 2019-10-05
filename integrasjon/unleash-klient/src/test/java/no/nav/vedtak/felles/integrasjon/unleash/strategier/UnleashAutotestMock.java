package no.nav.vedtak.felles.integrasjon.unleash.strategier;

import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.Variant;

import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Alternative
@Priority(1)
public class UnleashAutotestMock implements Unleash {
    protected final Logger LOG = LoggerFactory.getLogger(UnleashAutotestMock.class);

    private Map<String, Boolean> toggles = Map.ofEntries(
        Map.entry("fpoppdrag.inntrekk.fiks.sum",true),
        Map.entry("fpoppdrag.testgrensesnitt",true),
        Map.entry("fprisk.ereg.ansatte",false),
        Map.entry("fprisk.kafka.produsent",true),
        Map.entry("fprisk.kjor.regelsteg200",true),
        Map.entry("fprisk.kombiner.ansettelsesperidoer",false),
        Map.entry("fpsak.aksjonspunkt.faresignaler",true),
        Map.entry("fpsak.ap_dodfodt_80P_dekningsgrad",true),
        Map.entry("fpsak.arbeidsforhold.bruk.aktoer.id.mot.inntektskomp",true),
        Map.entry("fpsak.arbeidsforhold.bruk-rest-klient",true),
        Map.entry("fpsak.bestille_brev_fra_fpformidling",true),
        Map.entry("fpsak.bestill_manuell_brev_fra_fpformidling",true),
        Map.entry("fpsak.eksperimentelle_brev_fra_fpformidling",true),
        Map.entry("fpsak.forhaandsvis_brev_fra_fpformidling",true),
        Map.entry("fpsak.gradering.snfl",true),
        Map.entry("fpsak.hendelse.dod",true),
        Map.entry("fpsak.hendelse.dodfodsel",true),
        Map.entry("fpsak.hent_brevmaler_fra_fpformidling",true),
        Map.entry("fpsak.innhenting.ikke_sett_fp_soknad_atti_prosent_dekningsgrad",true),
        Map.entry("fpsak.klage-formkrav",true),
        Map.entry("fpsak.kontrollresultat.dto",true),
        Map.entry("fpsak.lagre_fritekst_inn_fpformidling",false),
        Map.entry("fpsak.overstyr_beregningsgrunnlag",true),
        Map.entry("fpsak.periodiseringBeregningsgrunnlagSVP",true),
        Map.entry("fpsak.pfp7761",true),
        Map.entry("fpsak.pfp7790",true),
        Map.entry("fpsak.reduserBeregningsgrunnlagSVP",false),
        Map.entry("fpsak.risikoklassifisering",true),
        Map.entry("fpsak.sende-kafka-hendelse",true),
        Map.entry("fpsak.send.kvitteringskoenavn",true),
        Map.entry("fpsak.send.tilkjentytelse",true),
        Map.entry("fpsak.simuler-oppdrag-varseltekst",true),
        Map.entry("fpsak.slaa-av-inntrekk",true),
        Map.entry("fpsak.sokt-for-tidlig-brev",true),
        Map.entry("fpsak.svp.autopunkter",true),
        Map.entry("fpsak.utled_ap_for_permisjon_i_5080",true),
        Map.entry("fpsak.vent-opptjening-brev",true),
        Map.entry("lagre.risikovurdering.innsyn",false)
    );

    @Override
    public boolean isEnabled(String toggleName) {
        return isEnabled(toggleName,false);
    }

    @Override
    public boolean isEnabled(String toggleName, boolean defaultSetting) {
        LOG.info("Sjekker test-verdi for toggle: {} - {}",
            toggleName, toggles.containsKey(toggleName) ? toggles.get(toggleName) : "Toggle-verdi ikke satt" );
        return toggles.getOrDefault(toggleName,defaultSetting);
    }

    @Override
    public List<String> getFeatureToggleNames() {
        return new ArrayList<>(toggles.keySet());
    }

	@Override
	public Variant getVariant(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Variant getVariant(String arg0, UnleashContext arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Variant getVariant(String arg0, Variant arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Variant getVariant(String arg0, UnleashContext arg1, Variant arg2) {
		throw new UnsupportedOperationException();
	}
}
