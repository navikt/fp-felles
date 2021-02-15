package no.nav.vedtak.feil;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

abstract class AbstractFeilTestIT {

    private static List<Class<? extends DeklarerteFeil>> deklarerteFeil = FeilUtil.finnAlleDeklarerteFeil();

    /**
     * Forventede prefix kommer an på bruken.
     * <p>
     * Tanken er at hvert git-repository kjører unikt prefix for å unngå unødvendig
     * styr ved konflikter. Dvs dette git-repositiet bruker 'F' som prefix,
     * foreldrepenger bruker 'FP' som prefix. Siden foreldrepenger bruker 'FP' som
     * prefix, må denne testen hos foreldrepenger inkludere både 'F' og 'FP'.
     */
    protected abstract List<String> getForventedePrefix();

    @Test
    void skal_ha_at_alle_metoder_i_interface_med_deklarerte_feil_har_nøyaktig_én_feilannotering() throws Exception {
        StringBuilder error = new StringBuilder();

        for (Class<? extends DeklarerteFeil> deklareringsinterface : deklarerteFeil) {
            for (Method method : deklareringsinterface.getDeclaredMethods()) {
                @SuppressWarnings("unchecked")
                List<Class<? extends Annotation>> annoteringer = finnAnnoteringer(method, TekniskFeil.class, FunksjonellFeil.class,
                        IntegrasjonFeil.class, ManglerTilgangFeil.class);
                if (annoteringer.isEmpty()) {
                    error.append("Mangler feil-annotering på ")
                            .append(prettyPrint(deklareringsinterface, method))
                            .append("\n");
                } else if (annoteringer.size() > 1) {
                    error.append("Det skal bare være én, men har følgende annoteringer på ")
                            .append(prettyPrint(deklareringsinterface, method))
                            .append(":")
                            .append(annoteringer)
                            .append("\n");
                }
            }
        }
        assertThat(error.toString()).isEmpty();
    }

    @Test
    void skal_ha_at_feilkoder_følger_mønster_slik_at_de_er_gjenkjennbare_og_unike_på_tvers_av_applikasjoner() throws Exception {
        String prefixMønster = "(" + String.join("|", getForventedePrefix()) + ")";
        String tallMønster = "\\d{6}";
        Pattern mønster = Pattern.compile("^" + prefixMønster + "-" + tallMønster + "$");
        StringBuilder error = new StringBuilder();
        for (Class<? extends DeklarerteFeil> deklareringsinterface : deklarerteFeil) {
            for (Method method : deklareringsinterface.getDeclaredMethods()) {
                String feilkode = FeilUtil.feilkode(method);
                if (feilkode == null) {
                    break; // håndtert av test som sjekker at alt er annotert
                }
                Matcher m = mønster.matcher(feilkode);
                if (!m.find()) {
                    error.append("Feilkoden " + feilkode + "følger ikke mønster\n");
                }
            }
        }
        assertThat(error.toString()).isEmpty();
    }

    @Test
    void skal_ha_unike_feilkoder_for_alle_deklarerte_feil() throws Exception {
        Map<String, List<InterfaceOgMetode>> feilPrFeilkode = new TreeMap<>();

        for (Class<? extends DeklarerteFeil> deklareringsinterface : deklarerteFeil) {
            for (Method method : deklareringsinterface.getDeclaredMethods()) {
                String feilkode = FeilUtil.feilkode(method);
                if (feilkode == null) {
                    break; // håndtert av test som sjekker at alt er annotert
                }
                if (!feilPrFeilkode.containsKey(feilkode)) {
                    feilPrFeilkode.put(feilkode, new ArrayList<>(1));
                }
                feilPrFeilkode.get(feilkode).add(new InterfaceOgMetode(deklareringsinterface, method));
            }
        }
        StringBuilder error = new StringBuilder();
        for (Map.Entry<String, List<InterfaceOgMetode>> entry : feilPrFeilkode.entrySet()) {
            if (entry.getValue().size() > 1) {
                error.append("Flere metoder som er deklarert med feilkoden ")
                        .append(entry.getKey())
                        .append(": ");
                boolean første = true;
                for (InterfaceOgMetode interfaceOgMetode : entry.getValue()) {
                    if (!første) {
                        error.append(" og ");
                    }
                    error.append(prettyPrint(interfaceOgMetode.getInterfacet(), interfaceOgMetode.getMetoden()));
                    første = false;
                }
                error.append("\n");
            }
        }
        assertThat(error.toString()).isEmpty();
    }

    @Test
    void skal_ha_pattern_for_alle_parametre_som_er_oppgitt() throws Exception {
        StringBuilder error = new StringBuilder();
        for (Class<? extends DeklarerteFeil> deklareringsinterface : deklarerteFeil) {
            for (Method method : deklareringsinterface.getDeclaredMethods()) {
                error.append(sjekkParametrisertStreng(deklareringsinterface, method));
            }
        }
        assertThat(error.toString()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private List<Class<? extends Annotation>> finnAnnoteringer(Method method, Class<? extends Annotation>... annoteringsklasser) {
        List<Class<? extends Annotation>> funnet = new ArrayList<>(1);
        for (Class<? extends Annotation> annoteringsklasse : annoteringsklasser) {
            if (method.isAnnotationPresent(annoteringsklasse)) {
                funnet.add(annoteringsklasse);
            }
        }
        return funnet;
    }

    private static String sjekkParametrisertStreng(Class<? extends DeklarerteFeil> deklareringsinterface, Method method) {
        String streng = FeilUtil.feilmelding(method);
        if (streng == null) {
            return ""; // sjekker i annen test at annotering er tilstede
        }
        Pattern gyldigParameter = Pattern.compile("%[1-9s]");
        Matcher m = gyldigParameter.matcher(streng);
        int count = 0;
        while (m.find()) {
            count++;
        }
        if (count != FeilUtil.tellParametreUtenomCause(method)) {
            return String.format(
                    "Parameter mismatch: Deklarert feil har %d parametre(%s), mens metoden har %d parametre (i tillegg til evt. cause som kan oppgis som siste parameter)",
                    count,
                    streng,
                    method.getParameterCount(),
                    prettyPrint(deklareringsinterface, method));
        }
        return "";
    }

    private static String prettyPrint(Class<?> klass, Method method) {
        StringBuilder b = new StringBuilder();
        b.append(klass.getName());
        b.append(".");
        b.append(method.getName());
        b.append("(");
        boolean first = true;
        for (Parameter parameter : method.getParameters()) {
            if (!first) {
                b.append(", ");
            }
            b.append(parameter.getType().getSimpleName());
            b.append(" ");
            b.append(parameter.getName());
            first = false;
        }
        b.append(")");
        return b.toString();
    }

    private static class InterfaceOgMetode {
        private Class<?> interfacet;
        private Method metoden;

        InterfaceOgMetode(Class<?> interfacet, Method metoden) {
            this.interfacet = interfacet;
            this.metoden = metoden;
        }

        Class<?> getInterfacet() {
            return interfacet;
        }

        Method getMetoden() {
            return metoden;
        }
    }

}
