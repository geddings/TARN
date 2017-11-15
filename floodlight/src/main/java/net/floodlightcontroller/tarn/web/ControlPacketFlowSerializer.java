package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.tarn.ControlPacketFlow;

import java.io.IOException;

/**
 * Created by @geddings on 11/15/17.
 */
public class ControlPacketFlowSerializer extends JsonSerializer<ControlPacketFlow> {
    @Override
    public void serialize(ControlPacketFlow packetFlow, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("source-ip", packetFlow.getSrcIp().toString());
        jgen.writeStringField("destination-ip", packetFlow.getDstIp().toString());
        jgen.writeStringField("in-port", packetFlow.getInPort().toString());
        jgen.writeStringField("out-port", packetFlow.getOutPort().toString());
        jgen.writeEndObject();
    }
}
