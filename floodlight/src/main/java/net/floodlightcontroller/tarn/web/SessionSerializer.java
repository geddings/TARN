package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.tarn.SessionImpl;

import java.io.IOException;

/**
 * Created by @geddings on 11/14/17.
 */
public class SessionSerializer extends JsonSerializer<SessionImpl> {
    @Override
    public void serialize(SessionImpl session, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeObjectField("inbound", session.getInbound());
        jgen.writeObjectField("outbound", session.getOutbound());
        jgen.writeEndObject();
    }
}
