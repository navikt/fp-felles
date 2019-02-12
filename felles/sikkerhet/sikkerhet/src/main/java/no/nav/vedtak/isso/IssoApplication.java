package no.nav.vedtak.isso;

import no.nav.vedtak.isso.ressurs.RelyingPartyCallback;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;

@ApplicationPath("cb")
public class IssoApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(RelyingPartyCallback.class);
    }

}
