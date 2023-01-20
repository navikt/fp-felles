package no.nav.vedtak.felles.testutilities.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CdiAwareExtension.class)
public class CdiExtensionTest {

    @Inject
    private AppBean appBean;

    @Inject
    private ReqBean requestBean;

    @Inject
    private DepBean depBean;

    @Test
    void gotApplicationScopedBean() {
        assertThat(appBean).isNotNull();
        assertThat(appBean.getClass()).isNotEqualTo(AppBean.class);
        assertThat(appBean.hello()).isEqualTo("app");
    }

    @Test
    void gotRequestScopedBean() {
        assertThat(requestBean).isNotNull();
        assertThat(requestBean.getClass()).isNotEqualTo(ReqBean.class);
        assertThat(requestBean.hello()).isEqualTo("request");
    }

    @Test
    void gotDependentScopedBean() {
        assertThat(depBean).isNotNull();
        assertThat(depBean.getClass()).isEqualTo(DepBean.class);
        assertThat(depBean.hello()).isEqualTo("dep");
    }
}


@ApplicationScoped
class AppBean {

    @Inject
    public AppBean() {

    }

    String hello() {
        return "app";
    }
}


@ApplicationScoped
class ReqBean {

    @Inject
    public ReqBean() {

    }

    String hello() {
        return "request";
    }
}


@Dependent
class DepBean {

    @Inject
    public DepBean() {

    }

    String hello() {
        return "dep";
    }
}
