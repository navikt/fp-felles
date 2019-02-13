package no.nav.vedtak.isso;

import java.io.IOException;
import java.time.Instant;
import java.util.Random;

import no.nav.vedtak.sikkerhet.oidc.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.domene.OidcCredential;
import no.nav.vedtak.sikkerhet.oidc.Fikk40xKodeException;
import no.nav.vedtak.sikkerhet.oidc.OidcLogin;
import no.nav.vedtak.sikkerhet.oidc.VlIOException;

/**
 * Når flere tråder på samme node forsøker å hente ID-token samtidig
 * feiler det fordi authorization coder som deles ut blir ugyldige før de tas i bruk.
 * Derfor bruker koden under synchronized.
 * <p>
 * Dette gjelder også når ulike noder forsøker å hente ID-token samtidig fra OpenAM.
 * En evt. lås for dette måtte ligge i databasen, det ønsket vi ikke. Derfor gjør
 * vi istedet kollisjonshåndtering her. Kollisjonshåndteringen er at ved kollisjon vil
 * en node vente en tilfeldig tid før den prøver på nytt.
 */
public class SystemUserIdTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(SystemUserIdTokenProvider.class);
    private static final Random RANDOM = new Random(); //NOSONAR brukes bare for å spre last, trenger ikke SecureRandom
    private static final int MAKS_ANTALL_FORSØK = 10;
    private static final int FORDELINGSFAKTOR = 10; //hvor mye nye forsøk fordeles ut i tid for å unngå konflikt
    static final long ESTIMERT_TID_FOR_Å_HENTE_TOKEN_MILLIS = 900L;

    private static OidcCredential idToken;
    private static Instant idTokenExpiryTime;

    private SystemUserIdTokenProvider() {
        //gjør SonarQube glad ved å hindre instansiering
    }

    public static synchronized OidcCredential getSystemUserIdToken() {
        if (idToken != null && idTokenExpiryTime.isAfter(Instant.now().plusMillis(OidcLogin.getMinimumTimeToExpiryBeforeRefresh()))) {
            return idToken;
        }

        idToken = fetchIdToken();
        idTokenExpiryTime = JwtUtil.getExpirationTime(idToken.getToken());
        return idToken;
    }

    private static OidcCredential fetchIdToken() {
        return fetchIdToken(1, new OpenAMHelper(), RANDOM);
    }

    static OidcCredential fetchIdToken(int forsøkNr, OpenAMHelper openAmHelper, Random random) {
        if (forsøkNr < MAKS_ANTALL_FORSØK - 1) {
            try {
                return fetchIdTokenDirect(openAmHelper);
            } catch (IOException | Fikk40xKodeException | VlIOException e) {
                long sovetid = sovetid(random);
                logger.info("Forsøk {} av {} for henting av systemuser id token feilet, fikk {}. Sover i {} ms og prøver på nytt", forsøkNr, MAKS_ANTALL_FORSØK, e, sovetid); //NOSONAR
                vent(sovetid);
                return fetchIdToken(forsøkNr + 1, openAmHelper, random);
            }
        } else {
            try {
                return fetchIdTokenDirect(openAmHelper);
            } catch (IOException e) {
                throw SystemUserIdTokenProviderFeil.FACTORY.klarteIkkeHenteIdTokenIOException(e).toException();
            } catch (VlIOException e) {
                throw SystemUserIdTokenProviderFeil.FACTORY.klarteIkkeHenteIdTokenVlIOException(e).toException();
            } catch (Fikk40xKodeException e) {
                throw SystemUserIdTokenProviderFeil.FACTORY.klarteIkkeHenteIdToken(MAKS_ANTALL_FORSØK, e).toException();
            }
        }
    }

    private static void vent(long sovetidMillis) {
        try {
            Thread.sleep(sovetidMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static long sovetid(Random random) {
        //Det viktigste her er at det soves minst like lenge som det forventes at det tar å hente token.
        //Når vi har havnet her, er det fordi to (eller) flere noder henter token samtidig
        //ved å minst vente så lenge, sikrer vi at den andre noden klarer å fullføre hentingen,
        //og da vil ikke den lage ny konflikt
        //ekstra fordeling utover i tid, er først og fremst for å unngå å gå i beina for innlogging
        //for batch-kjøringer
        return ESTIMERT_TID_FOR_Å_HENTE_TOKEN_MILLIS * (1L + random.nextInt(FORDELINGSFAKTOR));
    }

    private static OidcCredential fetchIdTokenDirect(OpenAMHelper openAmHelper) throws IOException {
        return openAmHelper.getToken()
                .getIdToken();
    }

}
