package net.floodlightcontroller.randomizer.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.randomizer.IHost;
import net.floodlightcontroller.randomizer.RandomizedHost;
import org.projectfloodlight.openflow.types.IPAddressWithMask;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by geddingsbarrineau on 10/29/16.
 */
public class RandomizedHostSerializer extends JsonSerializer<RandomizedHost> {
    @Override
    public void serialize(RandomizedHost randomizedHost, JsonGenerator jGen, SerializerProvider sProv)
            throws IOException, JsonProcessingException {
        jGen.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
        if (randomizedHost == null) {
            jGen.writeStartArray();
            jGen.writeString("No EAGER randomizedHost to report");
            jGen.writeEndArray();
            return;
        }
        jGen.writeStartObject();
        jGen.writeStringField("address", randomizedHost.getAddress(IHost.AddressType.INTERNAL).toString());
        jGen.writeStringField("randomized-address", randomizedHost.getRandomizedAddress().toString());
        jGen.writeStringField("prefix", randomizedHost.getPrefix().toString());
        jGen.writeObjectField("prefixes", randomizedHost.getPrefixes().stream().map(IPAddressWithMask::toString).collect(Collectors.toList()));
        jGen.writeEndObject();
    }
}
