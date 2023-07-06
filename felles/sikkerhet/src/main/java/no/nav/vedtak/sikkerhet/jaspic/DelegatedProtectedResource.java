package no.nav.vedtak.sikkerhet.jaspic;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthStatus;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Plugin interface for å håndtere beskyttelse delegert (eks. SOAP/Saml beskyttet ressurs).
 */
public interface DelegatedProtectedResource {

    /**
     * håndter resource, eller returner null dersom ingen authStatus kan angis.
     */
    Optional<AuthStatus> handleProtectedResource(HttpServletRequest request, Subject clientSubject, CallbackHandler containerCallbackHandler);

}
