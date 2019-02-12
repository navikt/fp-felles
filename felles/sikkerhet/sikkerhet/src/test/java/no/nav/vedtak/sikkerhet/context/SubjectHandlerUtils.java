package no.nav.vedtak.sikkerhet.context;

import no.nav.vedtak.sikkerhet.domene.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.domene.ConsumerId;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;

import javax.security.auth.Subject;

/**
 * Utilityclass that provides support for populating and resetting TestSubjectHandlers.
 */
public class SubjectHandlerUtils {

    /**
     * @see TestSubjectHandler#reset()
     */
    public static void reset() {
        final SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        if(subjectHandler instanceof TestSubjectHandler) {
            ((TestSubjectHandler) subjectHandler).reset();
        } else if(subjectHandler instanceof ThreadLocalSubjectHandler) {
          ((ThreadLocalSubjectHandler) subjectHandler).setSubject(null);
        } else {
            System.out.println("Don't know how to reset a SubjectHandler of type " + subjectHandler.getClass().getName());
        }
    }

    public static void setInternBruker(String userId) {
        setSubject(new SubjectBuilder(userId, IdentType.InternBruker).getSubject());
    }

    public static void setSubject(Subject subject) {
        final SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        if(subjectHandler instanceof TestSubjectHandler) {
            ((TestSubjectHandler) subjectHandler).setSubject(subject);
        } else if(subjectHandler instanceof ThreadLocalSubjectHandler) {
            ((ThreadLocalSubjectHandler) subjectHandler).setSubject(subject);
        } else {
            throw new IllegalStateException("Don't know how to set Subject on a SubjectHandler of type " + subjectHandler.getClass().getName());
        }
    }

    public static void useSubjectHandler(Class<? extends SubjectHandler> subjectHandlerClass) {
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, subjectHandlerClass.getName());
    }

    public static void unsetSubjectHandler() {
        System.clearProperty(SubjectHandler.SUBJECTHANDLER_KEY);
    }

    public static class SubjectBuilder {
        private String userId;
        private IdentType identType;
        private int authLevel;

        public SubjectBuilder(String userId, IdentType identType) {
            this.userId = userId;
            this.identType = identType;
            if (IdentType.InternBruker.equals(identType)) {
                authLevel = 4;
            }
        }

        public SubjectBuilder withAuthLevel(int authLevel) {
            this.authLevel = authLevel;
            return this;
        }

        public Subject getSubject() {
            Subject subject = new Subject();
            subject.getPrincipals().add(new SluttBruker(userId, identType));
            subject.getPublicCredentials().add(new AuthenticationLevelCredential(authLevel));
            subject.getPrincipals().add(new ConsumerId(SubjectHandlerUtils.class.getSimpleName()));
            return subject;
        }
    }
}