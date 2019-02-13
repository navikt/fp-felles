package no.nav.vedtak.sikkerhet.context;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.sikkerhet.domene.IdentType;

public class SubjectHandlerTest {

    private static final String USER_ID = "userId";
    private static final IdentType IDENT_TYPE = IdentType.InternBruker;
    private static final int AUTH_LEVEL = 4;

    @Rule
    public LogSniffer logSniffer = new LogSniffer(Level.DEBUG);

    @After
    public void clearSubjectHandler() {
        SubjectHandlerUtils.reset();
        SubjectHandlerUtils.unsetSubjectHandler();
    }

    @Test
    public void testGetDefaultSubjectHandler() {
        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();

        assertThat(subjectHandler, CoreMatchers.notNullValue());
        assertThat(subjectHandler, CoreMatchers.instanceOf(ThreadLocalSubjectHandler.class));
    }

    @Test
    public void testGetConfiguredSubjectHandler() {
        SubjectHandlerUtils.useSubjectHandler(StaticSubjectHandler.class);

        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();

        assertThat(subjectHandler, CoreMatchers.notNullValue());
        assertThat(subjectHandler, CoreMatchers.instanceOf(StaticSubjectHandler.class));
    }

    @Test
    public void testGetSubject() {
        SubjectHandlerUtils.useSubjectHandler(StaticSubjectHandler.class);
        SubjectHandlerUtils.setInternBruker(USER_ID);

        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();

        assertThat(subjectHandler, CoreMatchers.notNullValue());
        assertThat(subjectHandler.getUid(), CoreMatchers.is(USER_ID));
        assertThat(subjectHandler.getAuthenticationLevel(), CoreMatchers.is(AUTH_LEVEL));
        assertThat(subjectHandler.getIdentType(), CoreMatchers.is(IDENT_TYPE));
        assertThat(subjectHandler.getConsumerId(), CoreMatchers.is(SubjectHandlerUtils.class.getSimpleName()));
    }

}