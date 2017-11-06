package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.tarn.AutonomousSystem;
import org.projectfloodlight.openflow.types.IPAddressWithMask;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 7/30/17.
 */
@Deprecated
public class AutonomousSystemSerializer extends JsonSerializer<AutonomousSystem> {
    @Override
    public void serialize(AutonomousSystem as, JsonGenerator jGen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jGen.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);

        jGen.writeStartObject();
        jGen.writeNumberField("as-number", as.getASNumber());
        jGen.writeStringField("internal-prefix", as.getInternalPrefix().toString());
        jGen.writeStringField("external-prefix", as.getExternalPrefix().toString());
        jGen.writeObjectField("prefix-pool",as.getPrefixPool().stream().map(IPAddressWithMask::toString).collect(Collectors.toList()));
        jGen.writeEndObject();
    }
}
