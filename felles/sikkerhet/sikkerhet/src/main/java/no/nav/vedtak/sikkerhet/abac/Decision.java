package no.nav.vedtak.sikkerhet.abac;

public enum Decision {
	Permit("Permit"),
	Deny("Deny"),
	NotApplicable("NotApplicable"),
	Indeterminate("Indeterminate");

	private final String eksternKode;

	Decision(String eksternKode) {
		this.eksternKode = eksternKode;
	}

	public String getEksternKode() {
		return eksternKode;
	}
}
