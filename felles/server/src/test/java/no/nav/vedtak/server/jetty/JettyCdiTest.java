package no.nav.vedtak.server.jetty;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.Test;

class JettyCdiTest {

    @Test
    void injectsAndInterceptsRestResourceAndDestroysRequest() throws Exception {
        Greeting.DESTROYED = new CountDownLatch(1);
        GreetingInterceptor.INVOCATIONS.set(0);
        var server = JettyServerBuilder.builder().port(0).contextPath("/test")
            .registerRestApp("/api", RestApp.class).build();
        try (var client = HttpClient.newHttpClient()) {
            server.start();
            var port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
            var response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/test/api/greeting")).build(),
                HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("hello");
            assertThat(GreetingInterceptor.INVOCATIONS.get()).isEqualTo(1);
            assertThat(Greeting.DESTROYED.await(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            server.stop();
        }
        assertThat(server.isStopped()).isTrue();
    }

    public static class RestApp extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Set.of(GreetingResource.class);
        }
    }

    @Path("/greeting")
    @RequestScoped
    public static class GreetingResource {
        @Inject
        Greeting greeting;

        @GET
        public String hello() {
            return greeting.hello();
        }
    }

    @RequestScoped
    public static class Greeting {
        static CountDownLatch DESTROYED = new CountDownLatch(1);

        @Checked
        public String hello() {
            return "hello";
        }

        @PreDestroy
        void destroy() {
            DESTROYED.countDown();
        }
    }

    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface Checked {}

    @Checked
    @Interceptor
    @Priority(Interceptor.Priority.APPLICATION)
    public static class GreetingInterceptor {
        static final AtomicInteger INVOCATIONS = new AtomicInteger();

        @AroundInvoke
        public Object intercept(InvocationContext context) throws Exception {
            INVOCATIONS.incrementAndGet();
            return context.proceed();
        }
    }
}
