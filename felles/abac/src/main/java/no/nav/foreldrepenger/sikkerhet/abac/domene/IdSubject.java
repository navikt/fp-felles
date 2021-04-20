package no.nav.foreldrepenger.sikkerhet.abac.domene;

public class IdSubject {

    private final String subjectId;
    private final String subjectType;
    private final Integer subjectAuthLevel;

    private IdSubject(String subjectId, String subjectType, Integer authLevel) {
        this.subjectId = subjectId;
        this.subjectType = subjectType;
        this.subjectAuthLevel = authLevel;
    }

    public static IdSubject with(String id, String type, Integer level) {
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

    public Integer getSubjectAuthLevel() {
        return subjectAuthLevel;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [subject={subjectId='MASKERT', subjectType=" + subjectType + ", subjectLevel=" + subjectAuthLevel + "}]";
    }
}
