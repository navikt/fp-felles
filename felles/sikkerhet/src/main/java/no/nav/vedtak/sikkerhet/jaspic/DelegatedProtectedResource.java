package no.nav.vedtak.sikkerhet.jaspic;

import java.util.Optional;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthStatus;
import javax.servlet.http.HttpServletRequest;

/** Plugin interface for å håndtere beskyttelse delegert (eks. SOAP/Saml beskyttet ressurs). */
public interface DelegatedProtectedResource {

    /** håndter resource, eller returner null dersom ingen authStatus kan angis. */
    Optional<AuthStatus> handleProtectedResource(HttpServletRequest request, Subject clientSubject, CallbackHandler containerCallbackHandler);

}
