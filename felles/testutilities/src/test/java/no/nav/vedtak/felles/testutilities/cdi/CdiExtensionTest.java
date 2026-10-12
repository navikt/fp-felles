package no.nav.vedtak.felles.testutilities.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(CdiAwareExtension.class)
class CdiExtensionTest {

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

    @AfterAll
    static void destroyedInjectedDependents() {
        assertThat(DepBean.DESTROYED.get()).isEqualTo(DepBean.CREATED.get());
        assertThat(DepBean.CREATED.get()).isEqualTo(3);
    }
}


@ApplicationScoped
class AppBean {

    String hello() {
        return "app";
    }
}


@RequestScoped
class ReqBean {
    private final UUID id = UUID.randomUUID();

    UUID id() {
        return id;
    }

    String hello() {
        return "request";
    }
}


@Dependent
class DepBean {
    static final AtomicInteger CREATED = new AtomicInteger();
    static final AtomicInteger DESTROYED = new AtomicInteger();

    DepBean() {
        CREATED.incrementAndGet();
    }

    @PreDestroy
    void destroy() {
        DESTROYED.incrementAndGet();
    }

    String hello() {
        return "dep";
    }
}
