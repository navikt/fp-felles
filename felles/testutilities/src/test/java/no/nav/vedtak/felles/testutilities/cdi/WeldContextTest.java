package no.nav.vedtak.felles.testutilities.cdi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.spi.CDI;

import org.junit.jupiter.api.Test;

class WeldContextTest {

    @Test
    void nestedScopesPreserveTheOuterRequest() {
        var weld = WeldContext.getInstance();
        var first = weld.doWithScope(() -> {
            var id = request().id();
            assertThat(weld.doWithScope(() -> request().id())).isEqualTo(id);
            assertThat(request().id()).isEqualTo(id);
            return id;
        });
        assertInactive();
        assertThat(weld.doWithScope(() -> request().id())).isNotEqualTo(first);
        assertInactive();
    }

    @Test
    void exceptionsDestroyTheOwnedRequest() {
        var weld = WeldContext.getInstance();
        assertThatThrownBy(() -> weld.doWithScope(() -> {
            request().id();
            throw new IllegalStateException("test failure");
        })).isInstanceOf(IllegalStateException.class).hasMessage("test failure");
        assertInactive();
    }

    @Test
    void externallyOwnedRequestRemainsActive() {
        var weld = WeldContext.getInstance();
        try (var handle = CDI.current().select(RequestContextController.class).getHandle()) {
            var controller = handle.get();
            assertThat(controller.activate()).isTrue();
            try {
                var id = request().id();
                assertThat(weld.doWithScope(() -> request().id())).isEqualTo(id);
                assertThat(request().id()).isEqualTo(id);
            } finally {
                controller.deactivate();
            }
        }
        assertInactive();
    }

    @Test
    void workerThreadsHaveIndependentRequests() throws Exception {
        var weld = WeldContext.getInstance();
        var entered = new CountDownLatch(2);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> weld.doWithScope(() -> {
                var id = request().id();
                awaitBoth(entered);
                return id;
            }));
            var second = executor.submit(() -> weld.doWithScope(() -> {
                var id = request().id();
                awaitBoth(entered);
                return id;
            }));
            assertThat(first.get(10, TimeUnit.SECONDS)).isNotEqualTo(second.get(10, TimeUnit.SECONDS));
        }
        assertInactive();
    }

    private static void awaitBoth(CountDownLatch entered) {
        entered.countDown();
        try {
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static ReqBean request() {
        return CDI.current().select(ReqBean.class).get();
    }

    private static void assertInactive() {
        assertThatThrownBy(() -> CDI.current().getBeanManager().getContext(RequestScoped.class))
            .isInstanceOf(ContextNotActiveException.class);
    }
}
