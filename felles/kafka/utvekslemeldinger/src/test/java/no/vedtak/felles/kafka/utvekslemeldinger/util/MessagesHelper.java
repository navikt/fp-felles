package no.vedtak.felles.kafka.utvekslemeldinger.util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessagesHelper {

    public static Map<String, String> jsonMeldinger = new HashMap<>();

    public static void clearMessages() {
        jsonMeldinger.clear();
    }

    public static void lagMeldingMedBehandlinsfrist() {
        jsonMeldinger = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        jsonMeldinger.put("1",
            "{ \"behandlingId\": 1, \"fagsakId\": 1,\"behandlingsfrist\": \"" + LocalDateTime.now() + "\",\"aktiv\": \"" + Boolean.TRUE.toString() + "\"}");

        jsonMeldinger.put("2",
            "{ \"behandlingId\": 2, \"fagsakId\": 2,\"behandlingsfrist\": \"" + LocalDateTime.now() + "\",\"aktiv\": \"" + Boolean.TRUE.toString() + "\"}");

    }
}
