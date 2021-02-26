package no.nav.vedtak.isso;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import no.nav.vedtak.isso.ressurs.RelyingPartyCallback;

@ApplicationPath("cb")
public class IssoApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(RelyingPartyCallback.class);
    }

}
