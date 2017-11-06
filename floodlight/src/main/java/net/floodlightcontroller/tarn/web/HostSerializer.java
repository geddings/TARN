package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.tarn.Host;

import java.io.IOException;

/**
 * Created by geddingsbarrineau on 8/28/17.
 * 
 */
@Deprecated
public class HostSerializer extends JsonSerializer<Host> {
    @Override
    public void serialize(Host host, JsonGenerator jGen, SerializerProvider provider) throws IOException {
        jGen.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);

        jGen.writeStartObject();
        jGen.writeStringField("internal-address", host.getInternalAddress().toString());
        jGen.writeStringField("external-address", host.getExternalAddress().toString());
        jGen.writeNumberField("member-as", host.getMemberAS());
        jGen.writeEndObject();
    }
}
