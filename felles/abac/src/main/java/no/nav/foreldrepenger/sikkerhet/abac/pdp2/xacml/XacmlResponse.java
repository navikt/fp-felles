package no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml;

import java.util.List;

public record XacmlResponse(List<Response> Response) {
    public static record Response(no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision Decision, List<Assignments> AssociatedAdvice, List<Assignments> Obligations) {}
    public static record Assignments(String Id, List<AttributeAssignment> AttributeAssignment) {}
    public static record AttributeAssignment(String AttributeId, String Value) {}
}
