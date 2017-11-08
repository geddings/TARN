package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.tarn.PrefixMapping;

import java.io.IOException;

/**
 * Created by @geddings on 11/8/17.
 */
public class PrefixMappingSerializer extends JsonSerializer<PrefixMapping> {
    @Override
    public void serialize(PrefixMapping mapping, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("internal-ip", mapping.getInternalIp().toString());
        jgen.writeStringField("external-prefix", mapping.getCurrentPrefix().toString());
        jgen.writeEndObject();
    }
}
