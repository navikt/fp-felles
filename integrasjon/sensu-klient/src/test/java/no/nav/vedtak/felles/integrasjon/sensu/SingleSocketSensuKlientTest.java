package no.nav.vedtak.felles.integrasjon.sensu;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SingleSocketSensuKlientTest {

    private static ServerSocket serverSocket;
    private static SensuKlient sensuKlient;

    private BlockingQueue<String> socketOutput = new ArrayBlockingQueue<>(100);

    private static final String expectedJsonBeforeTimestamp = "{" +
        "\"name\":\"sensu-event-local-app\"," +
        "\"type\":\"metric\"," +
        "\"handlers\":[\"events_nano\"]," +
        "\"output\":\"local-app.registrert.task,application=local-app,cluster=local,namespace=default,task_type=task.registerSøknad counter=1i";

    @Before
    public void init() throws IOException {
        serverSocket = new ServerSocket(0);
        serverSocket.setSoTimeout(1000);
        sensuKlient = new SensuKlient("localhost", serverSocket.getLocalPort());
        sensuKlient.start();
        
        new Thread(() -> {
            try (Socket socket = serverSocket.accept()) {
                StringBuilder sb = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader
                  (socket.getInputStream(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                    int c = 0;
                    while ((c = reader.read()) != -1 && c != '\n') {
                        sb.append((char) c);
                    }
                }
                socketOutput.put(sb.toString());
            } catch (InterruptedException | IOException e) {
                throw new IllegalStateException("Kunne ikke lese fra socket", e);
            }
        }).start();
        

    }

    @After
    public void teardown() throws IOException {
        sensuKlient.stop();
        serverSocket.close();
    }

    @Test
    public void logMetrics() throws Exception {
        // Perform
        sensuKlient.logMetrics(SensuEvent.createSensuEvent(
            "registrert.task",
            Map.of("task_type", "task.registerSøknad"),
            Map.of("counter", 1)));

        // Assert
        String resultat = readFromSocket();
        assertThat(resultat).isNotNull();
        assertThat(resultat).startsWith(expectedJsonBeforeTimestamp);
    }

    private String readFromSocket() throws IOException, InterruptedException {
        return socketOutput.poll(10, TimeUnit.SECONDS);
    }
}