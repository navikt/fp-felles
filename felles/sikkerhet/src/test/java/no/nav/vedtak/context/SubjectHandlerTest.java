package no.nav.vedtak.context;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;
import no.nav.vedtak.sikkerhet.context.containers.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.context.containers.ConsumerId;
import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import org.junit.jupiter.api.Test;

import javax.security.auth.Subject;

import static org.assertj.core.api.Assertions.assertThat;

class SubjectHandlerTest {

    private static final String USER_ID = "userId";
    private static final IdentType IDENT_TYPE = IdentType.InternBruker;
    private static final int AUTH_LEVEL = 4;

    @Test
    void testGetDefaultSubjectHandler() {
        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        assertThat(subjectHandler).isNotNull();
        assertThat(subjectHandler).isInstanceOf(ThreadLocalSubjectHandler.class);
    }

    @Test
    void testGetSubject() {
        var eksisterende = SubjectHandler.getSubjectHandler().getSubject();
        ((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).setSubject(lagInternBruker(USER_ID));

        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        assertThat(subjectHandler).isNotNull();
        assertThat(subjectHandler.getAuthenticationLevel()).isEqualTo(AUTH_LEVEL);
        assertThat(subjectHandler.getIdentType()).isEqualTo(IDENT_TYPE);
        assertThat(subjectHandler.getConsumerId()).isEqualTo(SubjectHandlerTest.class.getSimpleName());

        ((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).setSubject(eksisterende);
    }

    private static Subject lagInternBruker(String userId) {
        var subject = new Subject();
        subject.getPrincipals().add(new SluttBruker(userId, IdentType.InternBruker));
        subject.getPublicCredentials().add(new AuthenticationLevelCredential(4));
        subject.getPrincipals().add(new ConsumerId(SubjectHandlerTest.class.getSimpleName()));
        return subject;
    }

}
