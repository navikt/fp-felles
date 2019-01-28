package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Path;

import org.junit.Test;

import no.nav.vedtak.sikkerhet.abac.ActionUthenter;

public class ActionUthenterTest {

    @Test
    public void skalLageActionForRestMethod() throws NoSuchMethodException {
        assertThat(ActionUthenter.action(MyRestSvc1.class, MyRestSvc1.class.getDeclaredMethod("myRestMethod1", String.class))).isEqualTo("/root1/resource1");
        assertThat(ActionUthenter.action(MyRestSvc1.class, MyRestSvc1.class.getDeclaredMethod("myRestMethod2", String.class))).isEqualTo("/root1/resource2");
        assertThat(ActionUthenter.action(MyRestSvc1.class, MyRestSvc1.class.getDeclaredMethod("myRestMethod3", String.class))).isEqualTo("/root1");
    }

    @Test
    public void skal_ha_at_action_for_webservice_er_action_i_webmethod() throws Exception {
        assertThat(ActionUthenter.action(MyWebService.class, MyWebService.class.getDeclaredMethod("coinToss"))).isEqualTo("http://foobar.com/biased/coin/toss/v1");
    }

    @Test
    public void skalLageActionForProsessTask() {
        assertThat(ActionUthenter.actionForProsessTask("doIt")).isEqualTo("doIt");
    }

    @Path("/root1")
    private static class MyRestSvc1 {
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
