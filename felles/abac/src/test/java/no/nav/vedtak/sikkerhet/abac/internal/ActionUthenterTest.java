package no.nav.vedtak.sikkerhet.abac.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.Path;

class ActionUthenterTest {

    @Test
    void skalLageActionForRestMethod() throws NoSuchMethodException {
        assertThat(ActionUthenter.action(MyRestSvc1.class, MyRestSvc1.class.getDeclaredMethod("myRestMethod1", String.class))).isEqualTo(
            "/root1/resource1");
        assertThat(ActionUthenter.action(MyRestSvc1.class, MyRestSvc1.class.getDeclaredMethod("myRestMethod2", String.class))).isEqualTo(
            "/root1/resource2");
        assertThat(ActionUthenter.action(MyRestSvc1.class, MyRestSvc1.class.getDeclaredMethod("myRestMethod3", String.class))).isEqualTo("/root1");
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

}
