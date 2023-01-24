package no.nav.vedtak.sikkerhet.kontekst;

import java.util.Objects;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum IdentType {
    // Case definert av NAV "standard". Brukes i ABAC policies. Til bruk i tokenprovider/obo-logikk
    Systemressurs, // Innkommende kall fra andre systembrukere
    EksternBruker, // Bruker 11/13 siffer
    InternBruker,  // Ansatt - matcher ident og etterhvert epost
    Samhandler,    // Annen organisasjon
    Sikkerhet,     // Ingen kjent bruk - potensielt ifm pip-requests ol.
    Prosess        // Ingen kjent bruk - foreslås brukt for prosesstasks
    ;

    private static final Logger LOG = LoggerFactory.getLogger(IdentType.class);

    private static final Pattern VALID_AKTØRID = Pattern.compile("^\\d{13}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_PERSONIDENT = Pattern.compile("^\\d{11}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_ANSATTIDENT = Pattern.compile("^\\w\\d{6}$", Pattern.CASE_INSENSITIVE);

    public boolean erSystem() {
        return Systemressurs.equals(this) || Prosess.equals(this);
    }

    public static IdentType utledIdentType(String uid) {
        if (Objects.equals(Systembruker.username(), uid)) {
            return IdentType.Systemressurs;
        } else if (uid != null && (VALID_AKTØRID.matcher(uid).matches() || VALID_PERSONIDENT.matcher(uid).matches())) {
            return IdentType.EksternBruker;
        } else if (uid != null && uid.startsWith("srv")) {
            return IdentType.Systemressurs;
        } else if (uid != null && VALID_ANSATTIDENT.matcher(uid).matches()) {
            return IdentType.InternBruker;
        }
        LOG.info("FPFELLES KONTEKST kunne ikke utlede identtype fra {}", uid);
        // TODO - her skal det strengt tatt være en exception .... Skal på sikt brukes til oppførsel for tokenprovider
        return IdentType.InternBruker;
    }
}
