package no.nav.vedtak.sikkerhet.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.domene.IdentType;

public class SubjectHandlerTest {

    private static final String USER_ID = "userId";
    private static final IdentType IDENT_TYPE = IdentType.InternBruker;
    private static final int AUTH_LEVEL = 4;

    @AfterEach
    public void clearSubjectHandler() {
        SubjectHandlerUtils.reset();
        SubjectHandlerUtils.unsetSubjectHandler();
    }

    @Test
    public void testGetDefaultSubjectHandler() {
        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        assertThat(subjectHandler).isNotNull();
        assertThat(subjectHandler).isInstanceOf(ThreadLocalSubjectHandler.class);
    }

    @Test
    public void testGetConfiguredSubjectHandler() {
        SubjectHandlerUtils.useSubjectHandler(StaticSubjectHandler.class);
        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        assertThat(subjectHandler).isNotNull();
        assertThat(subjectHandler).isInstanceOf(StaticSubjectHandler.class);
    }

    @Test
    public void testGetSubject() {
        SubjectHandlerUtils.useSubjectHandler(StaticSubjectHandler.class);
        SubjectHandlerUtils.setInternBruker(USER_ID);

        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        assertThat(subjectHandler).isNotNull();
        assertThat(subjectHandler.getAuthenticationLevel()).isEqualTo(AUTH_LEVEL);
        assertThat(subjectHandler.getIdentType()).isEqualTo(IDENT_TYPE);
        assertThat(subjectHandler.getConsumerId()).isEqualTo(SubjectHandlerUtils.class.getSimpleName());
    }

}