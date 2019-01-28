package no.nav.vedtak.feil;

import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class FeilDokumentasjon {

    public static void main(String[] args) {
        List<Class<? extends DeklarerteFeil>> feilene = FeilUtil.finnAlleDeklarerteFeil();

        System.out.println("Feilkode;Level;Type;Feilmelding;Løsningsforslag(hvis funksjonell feil);Parametre;Cause;Definert i Java-klasse;Java-metodenavn;");
        for (Class<? extends DeklarerteFeil> feilInterface : feilene) {
            for (Method method : feilInterface.getDeclaredMethods()) {
                System.out.println(skrivUtSemikolonseparertListe(feilInterface, method));
            }
        }
    }

    private static String skrivUtSemikolonseparertListe(Class<?> klass, Method method) {
        StringBuilder b = new StringBuilder();
        String skilletegn = ";";

        b.append(FeilUtil.feilkode(method));
        b.append(skilletegn);
        b.append(FeilUtil.logLevel(method));
        b.append(skilletegn);
        b.append(FeilUtil.type(method));
        b.append(skilletegn);
        b.append(FeilUtil.feilmelding(method));
        b.append(skilletegn);
        String løsningsforslag = FeilUtil.løsningsforslag(method);
        if (løsningsforslag != null) {
            b.append(løsningsforslag);
        }
        b.append(skilletegn);
        Parameter[] params = method.getParameters();
        int antallVanligeParametre = FeilUtil.tellParametreUtenomCause(method);
        for (int i = 0; i < antallVanligeParametre; i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(params[i].getType().getSimpleName());
        }
        b.append(skilletegn);
        if (FeilUtil.harMedCause(method)) {
            b.append(FeilUtil.deklarertCause(method));
        }
        b.append(skilletegn);
        b.append(klass.getName());
        b.append(skilletegn);
        b.append(method.getName());
        b.append(skilletegn);
        return b.toString();
    }
}
