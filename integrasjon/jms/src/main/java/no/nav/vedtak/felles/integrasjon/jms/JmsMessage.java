package no.nav.vedtak.felles.integrasjon.jms;

import java.util.HashMap;
import java.util.Map;

public class JmsMessage {

    private String text;
    private Map<String, String> headers;

    private JmsMessage(String message, Map<String, String> headers) {
        this.text = message;
        this.headers = headers;
    }

    public String getText() {
        return text;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean hasHeaders() {
        return !headers.isEmpty();
    }

    public static Builder builder() {
        return new JmsMessage.Builder();
    }

    public static class Builder {
        private String messageText;
        private Map<String, String> headers = new HashMap<>();

        public Builder withMessage(String messageText) {
            this.messageText = messageText;
            return this;
        }

        public Builder addHeader(String header, String value) {
            this.headers.put(header, value);
            return this;
        }

        public JmsMessage build() {
            return new JmsMessage(messageText, headers);
        }
    }
}
