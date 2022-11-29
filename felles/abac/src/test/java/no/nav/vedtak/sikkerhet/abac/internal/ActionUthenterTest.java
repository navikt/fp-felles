package no.nav.vedtak.sikkerhet.abac.internal;

import static org.assertj.core.api.Assertions.assertThat;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Path;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ServiceType;

class ActionUthenterTest {

    @Test
    void skalLageActionForRestMethod() throws NoSuchMethodException {
        assertThat(ActionUthenter.action(MyRestSvc1.class, MyRestSvc1.class.getDeclaredMethod("myRestMethod1", String.class)))
                .isEqualTo("/root1/resource1");
        assertThat(ActionUthenter.action(MyRestSvc1.class, MyRestSvc1.class.getDeclaredMethod("myRestMethod2", String.class)))
                .isEqualTo("/root1/resource2");
        assertThat(ActionUthenter.action(MyRestSvc1.class, MyRestSvc1.class.getDeclaredMethod("myRestMethod3", String.class))).isEqualTo("/root1");
    }

    @Test
    void skal_ha_at_action_for_webservice_er_action_i_webmethod() throws Exception {
        assertThat(ActionUthenter.action(MyWebService.class, MyWebService.class.getDeclaredMethod("coinToss"), ServiceType.WEBSERVICE))
                .isEqualTo("http://foobar.com/biased/coin/toss/v1");
    }

    @Path("/root1")
    static class MyRestSvc1 {
        @Path("/resource1")
        public void myRestMethod1(@SuppressWarnings("unused") String s) {
        }

        @Path("resource2")
        public void myRestMethod2(@SuppressWarnings("unused") String s) {
        }

        @SuppressWarnings("unused")
        public void myRestMethod3(String s) {
        }
    }

    @WebService
    private interface MyWebServiceInterface {
        @WebMethod(action = "http://foobar.com/biased/coin/toss/v1")
        boolean coinToss();
    }

    @WebService
    private static class MyWebService implements MyWebServiceInterface {
        @Override
        public boolean coinToss() {
            return false;
        }
    }

}
