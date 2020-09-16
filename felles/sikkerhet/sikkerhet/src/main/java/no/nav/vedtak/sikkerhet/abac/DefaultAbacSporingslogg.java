package no.nav.vedtak.sikkerhet.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

import org.slf4j.Logger;

import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.log.sporingslogg.SporingsloggId;
import no.nav.vedtak.log.sporingslogg.StandardSporingsloggId;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.util.AppLoggerFactory;

/**
 * Default eksempel på AbacSporingslogg implementasjon. Vil benyttes med mindre
 * applikasjonen definerer en {@link javax.enterprise.inject.Alternative}
 * implementasjon.
 *
 * Bør overrides i egen applikasjon dersom en har egne ABAC attributter eller
 * nøkler som skal spores. Her håndteres kun felles som
 * {@link NavAbacCommonAttributter} og {@link StandardSporingsloggId}.
 * Outputformat konsumeres av MF ArcSight.
 */
@Default
@ApplicationScoped
public class DefaultAbacSporingslogg implements AbacSporingslogg {

    private static final Logger SPORINGSLOGG = AppLoggerFactory.getSporingLogger(DefaultAbacSporingslogg.class);

    private static final char SPACE_SEPARATOR = ' ';

    private static <T> List<Sporingsdata> utvidSporingsdata(List<Sporingsdata> input, Collection<T> attributtVerdier, SporingsloggId id) {
        if (attributtVerdier.isEmpty()) {
            return input;
        }
        List<Sporingsdata> output = new ArrayList<>();
        for (Sporingsdata sporingsdata : input) {
            int i = 1;
            for (T attributtVerdi : attributtVerdier) {
                Sporingsdata sd = i++ < attributtVerdier.size()
                        ? sporingsdata.kopi()
                        : sporingsdata;
                output.add(sd.leggTilId(id, attributtVerdi.toString()));
            }
        }
        return output;
    }

    @Override
    public List<Sporingsdata> byggSporingsdata(Tilgangsbeslutning beslutning, AbacAttributtSamling attributter) {
        return byggSporingsdata(beslutning.getPdpRequest(), attributter);
    }

    @Override
    public void logg(List<Sporingsdata> sporingsdata) {
        for (Sporingsdata sporingsdatum : sporingsdata) {
            logg(sporingsdatum);
        }
    }

    @Override
    public void loggDeny(Tilgangsbeslutning beslutning, AbacAttributtSamling attributter) {
        loggDeny(beslutning.getPdpRequest(), beslutning.getDelbeslutninger(), attributter);
    }

    private static int antallSporingsrader(AbacAttributtSamling attributter) {
        return attributter.kryssProduktAntallAttributter();
    }

    private static List<Sporingsdata> byggIkkeSammensatteSporingsdata(AbacAttributtSamling attributter, List<Sporingsdata> pdpRequestSporingsdata) {
        // egne linjer, for å unngå store kryssprodukter
        List<Sporingsdata> resultat = new ArrayList<>();
        resultat.addAll(pdpRequestSporingsdata);
        resultat.addAll(leggPåAttributter(Sporingsdata.opprett(attributter.getAction()), attributter));
        return resultat;
    }

    private static List<Sporingsdata> byggSammensattSporingsdata(AbacAttributtSamling attributter, List<Sporingsdata> pdpRequestSporingsdata) {
        // logg på samme linje(r)
        List<Sporingsdata> resultat = new ArrayList<>();
        for (Sporingsdata sporingsdatum : pdpRequestSporingsdata) {
            resultat.addAll(leggPåAttributter(sporingsdatum, attributter));
        }
        return resultat;
    }

    private List<Sporingsdata> byggSporingsdata(PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        int antallRaderFraPdpRequest = antallResources(pdpRequest);
        List<Sporingsdata> pdpRequestSporingsdata = new ArrayList<>();
        for (int i = 0; i < antallRaderFraPdpRequest; i++) {
            pdpRequestSporingsdata.add(byggSporingsdata(attributter.getAction(), pdpRequest, i));
        }

        int antallRaderFraAttributter = antallSporingsrader(attributter);
        return antallRaderFraAttributter == 1 || antallRaderFraPdpRequest == 1
                ? byggSammensattSporingsdata(attributter, pdpRequestSporingsdata)
                : byggIkkeSammensatteSporingsdata(attributter, pdpRequestSporingsdata);
    }

    private Sporingsdata byggSporingsdata(String action, PdpRequest pdpRequest, int index, Decision decision) {
        return byggSporingsdata(action, pdpRequest, index).leggTilId(StandardSporingsloggId.ABAC_DECISION, decision.getEksternKode());
    }

    private static String fjernMellomrom(String verdi) {
        return verdi != null ? verdi.replace(' ', '_') : null;
    }

    private static List<Sporingsdata> leggPåAttributter(Sporingsdata original, AbacAttributtSamling attributter) {
        List<Sporingsdata> sporingsdata = new ArrayList<>();
        sporingsdata.add(original);

        for (var attrib : attributter.keySet()) {
            sporingsdata = utvidSporingsdata(sporingsdata, attributter.getVerdier(attrib), attrib);
        }

        return sporingsdata;
    }

    private static void logg(Sporingsdata sporingsdata) {
        StringBuilder msg = new StringBuilder()
                .append("action=").append(sporingsdata.getAction()).append(SPACE_SEPARATOR);
        for (var entry : sporingsdata.entrySet()) {
            String nøkkel = entry.getKey();
            String verdi = entry.getValue();
            msg.append(nøkkel)
                    .append('=')
                    .append(fjernMellomrom(verdi))
                    .append(SPACE_SEPARATOR);
        }
        String sanitizedMsg = LoggerUtils.toStringWithoutLineBreaks(msg);
        SPORINGSLOGG.info(sanitizedMsg);
    }

    /** Antall identer (aktørId, fnr) som behandles i denne requesten. */
    protected int antallIdenter(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)
                + pdpRequest.getAntall(no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR);
    }

    protected int antallResources(PdpRequest pdpRequest) {
        int antallIdenter = antallIdenter(pdpRequest);
        int antallResources = getAntallResources(pdpRequest);
        return Math.max(1, antallIdenter) * Math.max(1, antallResources);
    }

    protected Sporingsdata byggSporingsdata(String action, PdpRequest pdpRequest, int index) {
        Sporingsdata sporingsdata = Sporingsdata.opprett(action)
                .leggTilId(StandardSporingsloggId.ABAC_ACTION,
                        pdpRequest.getString(no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID))
                .leggTilId(StandardSporingsloggId.ABAC_RESOURCE_TYPE,
                        pdpRequest.getString(no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE));

        // hent ut fnr og aksjonpspunkt-typer vha indexer pga kryssprodukt mellom disse
        setOptionalListValueinAttributeSet(sporingsdata, pdpRequest, no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR,
                index % Math.max(pdpRequest.getAntall(no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR), 1),
                StandardSporingsloggId.FNR);

        setOptionalListValueinAttributeSet(sporingsdata, pdpRequest,
                no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE,
                index % Math.max(pdpRequest.getAntall(no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE),
                        1),
                StandardSporingsloggId.AKTOR_ID);

        setCustomSporingsdata(pdpRequest, index, sporingsdata);
        return sporingsdata;
    }

    /**
     * Eks. antall akjonspunkter, mottate dokumenter, el. som behandles i denne
     * requesten.
     */
    protected int getAntallResources(@SuppressWarnings("unused") PdpRequest pdpRequest) {
        return 1; // default - override etter behov
    }

    protected void loggDeny(PdpRequest pdpRequest, List<Decision> decisions, AbacAttributtSamling attributter) {
        List<Sporingsdata> pdpRequestSporingsdata = new ArrayList<>();
        for (int i = 0; i < decisions.size(); i++) {
            if (decisions.get(i) == Decision.Deny) {
                pdpRequestSporingsdata.add(byggSporingsdata(attributter.getAction(), pdpRequest, i, decisions.get(i)));
            }
        }

        List<Sporingsdata> sporingsdata = byggSammensattSporingsdata(attributter, pdpRequestSporingsdata);
        logg(sporingsdata);
    }

    protected void loggTilgang(AbacAttributtSamling attributter) {
        loggTilgang(new PdpRequest(), attributter);
    }

    protected void loggTilgang(PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        List<Sporingsdata> sporingsdata = byggSporingsdata(pdpRequest, attributter);
        logg(sporingsdata);
    }

    @SuppressWarnings("unused")
    protected void setCustomSporingsdata(PdpRequest pdpRequest, int index, Sporingsdata sporingsdata) {
        // Template method

        // bruk følgende til å legge til sporingsdata egne attributter
        // setOptionalListValueinAttributeSet
        // setOptionalValueinAttributeSet

    }

    /** Hjelpe metode for å legge til sporingsdata. */
    protected void setOptionalListValueinAttributeSet(Sporingsdata sporingsdata, PdpRequest pdpRequest, String key, int index, SporingsloggId id) {
        List<String> list = pdpRequest.getListOfString(key);
        if (list.size() >= index + 1) {
            Optional.ofNullable(list.get(index)).ifPresent(s -> sporingsdata.leggTilId(id, s));
        }
    }

    /** Hjelpe metode for å legge til sporingsdata. */
    protected void setOptionalValueinAttributeSet(Sporingsdata sporingsdata, PdpRequest pdpRequest, String key, SporingsloggId id) {
        pdpRequest.getOptional(key).ifPresent(s -> sporingsdata.leggTilId(id, s));
    }

}
