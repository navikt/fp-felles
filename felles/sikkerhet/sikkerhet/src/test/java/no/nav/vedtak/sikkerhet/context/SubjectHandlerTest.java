package no.nav.vedtak.sikkerhet.context;

import static org.assertj.core.api.Assertions.assertThat;

import javax.security.auth.Subject;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.domene.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.domene.ConsumerId;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;

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
        ((ThreadLocalSubjectHandler)SubjectHandler.getSubjectHandler()).setSubject(lagInternBruker(USER_ID));

        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        assertThat(subjectHandler).isNotNull();
        assertThat(subjectHandler.getAuthenticationLevel()).isEqualTo(AUTH_LEVEL);
        assertThat(subjectHandler.getIdentType()).isEqualTo(IDENT_TYPE);
        assertThat(subjectHandler.getConsumerId()).isEqualTo(SubjectHandlerTest.class.getSimpleName());

        ((ThreadLocalSubjectHandler)SubjectHandler.getSubjectHandler()).setSubject(eksisterende);
    }

    private static Subject lagInternBruker(String userId) {
        var subject = new Subject();
        subject.getPrincipals().add(new SluttBruker(userId, IdentType.InternBruker));
        subject.getPublicCredentials().add(new AuthenticationLevelCredential(4));
        subject.getPrincipals().add(new ConsumerId(SubjectHandlerTest.class.getSimpleName()));
        return subject;
    }

}
