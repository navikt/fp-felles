package no.nav.vedtak.util;

public class InputValideringRegex {
    private static final String REGEXP_START = "^[";
    private static final String REGEXP_SLUTT = "]*$";

    private static final String ALFABET_ENGELSK = "a-zA-Z";
    private static final String ALFABET_NORSK = ALFABET_ENGELSK + "æøåÆØÅ";
    private static final String ALFABET_ALLE = "\\p{L}";
    private static final String TALL = "\\p{Digit}";
    private static final String ANDRE_TEGN_NAVN = "\\p{Blank}.'\\-\\–"; // eksempler: Jan-Ole O'Brian Jr.
    private static final String ANDRE_TEGN_ADRESSE = "/\\p{Space}"; // eksempler: c/o

    private static final String TEGN_NAVN = TALL + ALFABET_ALLE + ANDRE_TEGN_NAVN; //TODO bør fjerne tall, men syntetiske brukere i test har tall her
    private static final String TEGN_ADRESSE = TEGN_NAVN + ANDRE_TEGN_ADRESSE;
    private static final String TEGN_FRITEKST = TEGN_ADRESSE + "%§\\!?@_()+:;,=\"&\\p{Sc}";

    /**
     * Bruk dette mønsteret for å validere koder i kodeverk.
     * <p>
     * Koder i kodeverk forventers å være kombinasjon av tall, bokstaver, underscore og bindestrek.
     */
    public static final String KODEVERK = REGEXP_START + ALFABET_NORSK +"_\\-\\–" + TALL + REGEXP_SLUTT; //TODO Bør fjerne æøåÆØÅ fra kodeverk, men det er i bruk nå

    /**
     * Bruk dette mønsteret for å validere navn på personer eller enheter.
     * <p>
     * Godtdar følgende i navn: <ul>
     * <li>tegn fra det norske alfabetet</li>
     * <li>tegn fra det samiske alfabetet</li>
     * <li>mellomrom, punktum, bindestreg og enkeltfnutt</li>
     * </ul>
     */
    public static final String NAVN = REGEXP_START + TEGN_NAVN + REGEXP_SLUTT;

    /**
     * Bruk dette mønsteret for å validere adresser.
     * <p>
     * Godtdar følgende i adresse
     * <ul>
     * <li>alt som godtas som navn</li>
     * <li>tall</li>
     * <li>skråstrek (for å støtte c/o), linjeskift</li>
     * </ul>
     */
    public static final String ADRESSE = REGEXP_START + TEGN_ADRESSE + REGEXP_SLUTT;

    /**
     * Bruk dette mønsteret for å validere fritekst.
     * <p>
     * Godtar i tillegg til alt som er tillatt i navn og adresser også flere andre tegn som er relevante.
     *
     * Godtar ikke større-enn og mindre-enn tegn da disse kan misbrukes til å gjøre XSS-angrep
     */
    public static final String FRITEKST = REGEXP_START + TEGN_FRITEKST + REGEXP_SLUTT;

    /**
     * Bruk dette mønsteret for å validere BASE64 "URL and Filename safe".
     *
     * @see java.util.Base64
     * @see java.util.Base64#getUrlEncoder()
     * @see <a href="https://tools.ietf.org/html/rfc4648#section-5">RFC 4648 : Base 64 Encoding with URL and Filename Safe Alphabet</a>
     */
    public static final String BASE64_RFC4648_URLSAFE_WITH_PADDING = REGEXP_START + ALFABET_ENGELSK + TALL + "\\-_=" + REGEXP_SLUTT;

    private InputValideringRegex() {
        throw new IllegalAccessError("Skal ikke instansieres");
    }
}
