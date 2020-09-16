package no.nav.vedtak.sikkerhet;

import java.lang.reflect.Method;
import java.util.List;

import javax.security.auth.Subject;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.context.SubjectHandlerUtils;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.OidcCredential;

public class InnloggetSubjectExtension implements InvocationInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(InnloggetSubjectExtension.class);

    private String ident = "A000000";
    private IdentType identType = IdentType.InternBruker;
    private List<Object> publicCredentials = List.of(new OidcCredential("dummy.oidc.token"));

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        try {
            SubjectHandlerUtils.useSubjectHandler(ThreadLocalSubjectHandler.class);
            SubjectHandlerUtils.setSubject(buildSubject());
            invocation.proceed();
        } catch (Exception e) {
            LOG.warn("OOPS", e);
        } finally {
            SubjectHandlerUtils.unsetSubjectHandler();
        }
    }

    private Subject buildSubject() {
        Subject subject = new SubjectHandlerUtils.SubjectBuilder(ident, identType).getSubject();
        for (Object publicCredential : publicCredentials) {
            subject.getPublicCredentials().add(publicCredential);
        }
        return subject;

    }
}
