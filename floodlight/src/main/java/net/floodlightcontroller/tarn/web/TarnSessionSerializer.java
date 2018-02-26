package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.tarn.TarnSession;

import java.io.IOException;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/26/18.
 */
public class TarnSessionSerializer extends JsonSerializer<TarnSession> {

    @Override
    public void serialize(TarnSession session, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("direction", session.getDirection().name());
        jgen.writeStringField("ip-protocol", session.getIpProtocol().toString());
        jgen.writeStringField("in-port", session.getInPort().toString());
        jgen.writeStringField("out-port", session.getOutPort().toString());
        jgen.writeStringField("internal-src-ip", session.getInternalSrcIp().toString());
        jgen.writeStringField("internal-dst-ip", session.getInternalDstIp().toString());
        jgen.writeStringField("external-src-ip", session.getExternalSrcIp().toString());
        jgen.writeStringField("external-dst-ip", session.getExternalDstIp().toString());
        jgen.writeEndObject();
    }
}
