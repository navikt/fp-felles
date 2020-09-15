package no.nav.vedtak.sikkerhet.context;

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

        // assertThat(subjectHandler, CoreMatchers.notNullValue());
        // assertThat(subjectHandler,
        // CoreMatchers.instanceOf(ThreadLocalSubjectHandler.class));
    }

    @Test
    public void testGetConfiguredSubjectHandler() {
        SubjectHandlerUtils.useSubjectHandler(StaticSubjectHandler.class);

        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();

        // assertThat(subjectHandler, CoreMatchers.notNullValue());
        // assertThat(subjectHandler,
        // CoreMatchers.instanceOf(StaticSubjectHandler.class));
    }

    @Test
    public void testGetSubject() {
        SubjectHandlerUtils.useSubjectHandler(StaticSubjectHandler.class);
        SubjectHandlerUtils.setInternBruker(USER_ID);

        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();

        /*
         * assertThat(subjectHandler, CoreMatchers.notNullValue());
         * assertThat(subjectHandler.getUid(), CoreMatchers.is(USER_ID));
         * assertThat(subjectHandler.getAuthenticationLevel(),
         * CoreMatchers.is(AUTH_LEVEL)); assertThat(subjectHandler.getIdentType(),
         * CoreMatchers.is(IDENT_TYPE)); assertThat(subjectHandler.getConsumerId(),
         * CoreMatchers.is(SubjectHandlerUtils.class.getSimpleName()));
         */
    }

}