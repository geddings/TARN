package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.tarn.Session;

import java.io.IOException;

/**
 * Created by @geddings on 11/14/17.
 */
public class SessionSerializer extends JsonSerializer<Session> {
    @Override
    public void serialize(Session session, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeObjectField("inbound", session.getInbound());
        jgen.writeObjectField("outbound", session.getOutbound());
        jgen.writeEndObject();
    }
}
