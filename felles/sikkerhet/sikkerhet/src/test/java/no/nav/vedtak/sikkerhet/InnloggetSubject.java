package no.nav.vedtak.sikkerhet;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import no.nav.vedtak.sikkerhet.context.SubjectHandlerUtils;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.OidcCredential;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;

public class InnloggetSubject implements MethodRule {

    private String ident = "A000000";
    private IdentType identType = IdentType.InternBruker;
    private List<Object> publicCredentials = new ArrayList<>();

    public InnloggetSubject medIdent(String ident) {
        this.ident = ident;
        return this;
    }

    public InnloggetSubject medIdentType(IdentType identType) {
        this.identType = identType;
        return this;
    }

    public InnloggetSubject medOidcToken(String oidcToken) {
        return medOidcToken(new OidcCredential(oidcToken));
    }

    public InnloggetSubject medOidcToken(OidcCredential oidcToken) {
        return addPublicCredential(oidcToken);
    }

    public InnloggetSubject medSamlToken(SAMLAssertionCredential samlToken) {
        return addPublicCredential(samlToken);
    }

    public InnloggetSubject addPublicCredential(Object credential) {
        this.publicCredentials.add(credential);
        return this;
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                SubjectHandlerUtils.useSubjectHandler(ThreadLocalSubjectHandler.class);
                Subject subject = buildSubject();
                SubjectHandlerUtils.setSubject(subject);

                base.evaluate();

                SubjectHandlerUtils.unsetSubjectHandler();
            }
        };
    }

    private Subject buildSubject() {
        Subject subject = new SubjectHandlerUtils.SubjectBuilder(ident, identType).getSubject();
        for (Object publicCredential : publicCredentials) {
            subject.getPublicCredentials().add(publicCredential);
        }
        return subject;

    }
}
