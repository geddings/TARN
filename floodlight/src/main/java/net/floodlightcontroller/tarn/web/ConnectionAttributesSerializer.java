package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.tarn.ConnectionAttributes;

import java.io.IOException;

/**
 * Created by @geddings on 11/14/17.
 */
public class ConnectionAttributesSerializer extends JsonSerializer<ConnectionAttributes> {
    @Override
    public void serialize(ConnectionAttributes connection, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("source-ip", connection.getSrcIp().toString());
        jgen.writeStringField("destination-ip", connection.getDstIp().toString());
        jgen.writeStringField("source-port", connection.getSrcPort().toString());
        jgen.writeStringField("destination-port", connection.getDstPort().toString());
        jgen.writeStringField("in-port", connection.getInPort().toString());
        jgen.writeStringField("out-port", connection.getOutPort().toString());
        jgen.writeEndObject();
    }
}
