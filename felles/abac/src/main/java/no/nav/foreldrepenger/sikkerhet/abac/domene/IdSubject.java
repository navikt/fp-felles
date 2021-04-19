package no.nav.foreldrepenger.sikkerhet.abac.domene;

import java.util.Optional;

public class IdSubject {

    private final String subjectId;
    private final String subjectLevel;
    private final String subjectType;

    private IdSubject(String subjectId, String subjectType, String subjectLevel) {
        this.subjectId = subjectId;
        this.subjectType = subjectType;
        this.subjectLevel = subjectLevel;
    }

    public static IdSubject with(String id, String type, String level) {
        return new IdSubject(id, type, level);
    }

    public static IdSubject with(String id, String type) {
        return with(id, type, null);
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public Optional<String> getSubjectLevel() {
        return Optional.ofNullable(subjectLevel);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [subject={subjectId='MASKERT', subjectType=" + subjectType + ", subjectLevel=" + subjectLevel + "}]";
    }
}
