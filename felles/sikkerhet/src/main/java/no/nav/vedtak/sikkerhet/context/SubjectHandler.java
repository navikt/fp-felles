package no.nav.vedtak.sikkerhet.context;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.context.containers.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.context.containers.ConsumerId;
import no.nav.vedtak.sikkerhet.context.containers.OidcCredential;
import no.nav.vedtak.sikkerhet.context.containers.SAMLAssertionCredential;
import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public abstract class SubjectHandler {
    public abstract Subject getSubject();

    public static SubjectHandler getSubjectHandler() {
        return new JettySubjectHandler();
    }

    public String getUid() {
        return getUid(getSubject());
    }

    public static String getUid(Subject subject) {
        return Optional.ofNullable(getSluttBruker(subject)).map(SluttBruker::getName).orElse(null);
    }

    public SluttBruker getSluttBruker() {
        return getSluttBruker(getSubject());
    }

    public static SluttBruker getSluttBruker(Subject subject) {
        return Optional.ofNullable(subject).map(s -> s.getPrincipals(SluttBruker.class)).map(SubjectHandler::getTheOnlyOneInSet).orElse(null);
    }

    public IdentType getIdentType() {
        return Optional.ofNullable(getSubject())
            .map(s -> s.getPrincipals(SluttBruker.class))
            .map(SubjectHandler::getTheOnlyOneInSet)
            .map(SluttBruker::getIdentType)
            .orElse(null);
    }

    public String getInternSsoToken() {
        return Optional.ofNullable(getSubject())
            .map(s -> s.getPublicCredentials(OidcCredential.class))
            .map(SubjectHandler::getTheOnlyOneInSet)
            .filter(Objects::nonNull)
            .map(OidcCredential::getToken)
            .orElse(null);
    }

    public OpenIDToken getOpenIDToken() {
        return Optional.ofNullable(getSubject())
            .map(s -> s.getPublicCredentials(OidcCredential.class))
            .map(SubjectHandler::getTheOnlyOneInSet)
            .filter(Objects::nonNull)
            .map(OidcCredential::getOpenIDToken)
            .orElse(null);
    }

    public SAMLAssertionCredential getSamlToken() {
        return Optional.ofNullable(getSubject())
            .map(s -> s.getPublicCredentials(SAMLAssertionCredential.class))
            .map(SubjectHandler::getTheOnlyOneInSet)
            .orElse(null);
    }

    public Integer getAuthenticationLevel() {
        return Optional.ofNullable(getSubject())
            .map(s -> s.getPublicCredentials(AuthenticationLevelCredential.class))
            .map(SubjectHandler::getTheOnlyOneInSet)
            .filter(Objects::nonNull)
            .map(AuthenticationLevelCredential::getAuthenticationLevel)
            .orElse(null);
    }

    public String getConsumerId() {
        return getConsumerId(getSubject());
    }

    public static String getConsumerId(Subject subject) {
        return Optional.ofNullable(subject)
            .map(s -> s.getPrincipals(ConsumerId.class))
            .map(SubjectHandler::getTheOnlyOneInSet)
            .filter(Objects::nonNull)
            .map(ConsumerId::getConsumerId)
            .orElse(null);
    }

    private static <T> T getTheOnlyOneInSet(Set<T> set) {
        if (set.isEmpty()) {
            return null;
        }

        if (set.size() == 1) {
            return set.iterator().next();
        }

        // logging class names to the log to help debug. Cannot log actual objects,
        // since then ID_tokens may be logged
        Set<String> classNames = set.stream().map(Object::getClass).map(Class::getName).collect(Collectors.toSet());
        throw new TekniskException("F-327190",
            String.format("Forventet ingen eller ett element, men fikk %s elementer av type %s", set.size(), classNames));
    }

}
