package no.nav.vedtak.felles.integrasjon.sensu;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SensuKlientTest {

    private static ServerSocket serverSocket;
    private static final String expectedJsonBeforeTimestamp = "{" +
            "\"name\":\"sensu-event-local-app\"," +
            "\"type\":\"metric\"," +
            "\"handlers\":[\"events_nano\"]," +
            "\"output\":\"local-app.registrert.task,application=local-app,cluster=local,namespace=default,task_type=task.registerSøknad counter=1i";

    @BeforeClass
    public static void init() throws IOException {
        serverSocket = new ServerSocket(0);
    }

    @AfterClass
    public static void teardown() throws IOException {
        serverSocket.close();
    }

    @Test
    public void logMetrics() throws IOException {
        SensuKlient sensuKlient = new SensuKlient("localhost", serverSocket.getLocalPort());
        SensuEvent sensuDto = SensuEvent.createSensuEvent(
                "registrert.task",
                Map.of("task_type", "task.registerSøknad"),
                Map.of("counter", 1));

        //Perform
        sensuKlient.logMetrics(sensuDto);

        //Assert
        String jsonString = readFromSocket();
        assertThat(jsonString).startsWith(expectedJsonBeforeTimestamp);
    }

    private String readFromSocket() throws IOException {
        serverSocket.setSoTimeout(1000);
        try (Socket socket = serverSocket.accept()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return reader.lines().collect(Collectors.joining(""));
        }
    }
}