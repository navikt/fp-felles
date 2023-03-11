package no.nav.vedtak.felles.integrasjon.dokarkiv.ser;

import java.io.IOException;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ByteArraySomBase64StringSerializer extends JsonSerializer<byte[]> {

    @Override
    public void serialize(byte[] o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (o != null) {
            String string = Base64.getEncoder().encodeToString(o);
            jsonGenerator.writeString(string);
        }
    }
}
