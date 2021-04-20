package no.nav.foreldrepenger.sikkerhet.abac.pdp.xacml;

import javax.json.JsonObject;

public class Obligation {
    private static final String ID_KEY = "Id";

    private final String id;
    private final JsonObject jsonObject;

    public Obligation(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        this.id = jsonObject.getString(ID_KEY);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
