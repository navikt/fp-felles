package no.nav.vedtak.felles.integrasjon.sensu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.logging.MDC;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import no.nav.vedtak.log.mdc.MDCOperations;

public class SensuKlientTest {

    private static ServerSocket serverSocket;
    private static SensuKlient sensuKlient;

    private static final String expectedJsonBeforeTimestamp = "{" +
            "\"name\":\"sensu-event-local-app\"," +
            "\"type\":\"metric\"," +
            "\"handlers\":[\"events_nano\"]," +
            "\"output\":\"local-app.registrert.task,application=local-app,cluster=local,namespace=default,task_type=task.registerSøknad counter=1i";

    int ANTALL_MELDINGER_SENDT = 100;

    @BeforeClass
    public static void init() throws IOException {
        serverSocket = new ServerSocket(0);
        sensuKlient = new SensuKlient("localhost", serverSocket.getLocalPort());
        sensuKlient.start();
    }

    @AfterClass
    public static void teardown() throws IOException {
        serverSocket.close();
        sensuKlient.stop();
    }

    @Test
    public void logMetrics() throws IOException {
        //Perform
        for (int i = 0; i < ANTALL_MELDINGER_SENDT; i++) {
            MDCOperations.putCallId("Sender: " + i);
            sensuKlient.logMetrics(SensuEvent.createSensuEvent(
                    "registrert.task",
                    Map.of("task_type", "task.registerSøknad"),
                    Map.of("counter", 1)));
        }

        //Assert
        List<String> resultat = readFromSocket();
        assertThat(resultat).isNotNull();
        assertThat(resultat).isNotEmpty();
        assertThat(resultat.size()).isEqualTo(ANTALL_MELDINGER_SENDT);

        for (int i = 0; i < ANTALL_MELDINGER_SENDT; i++) {
            assertThat(resultat.get(i)).startsWith(expectedJsonBeforeTimestamp);
        }
    }

    private List<String> readFromSocket() throws IOException {
        serverSocket.setSoTimeout(1000);
        List<String> result = new ArrayList<>();
        try (Socket socket = serverSocket.accept()) {
            System.out.println("A new client is connected : " + socket);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int i = 0;
            while (i < ANTALL_MELDINGER_SENDT) {
                try {
                    String received = reader.readLine();
                    System.out.println("Got: " + received);
                    result.add(received);
                    i++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}