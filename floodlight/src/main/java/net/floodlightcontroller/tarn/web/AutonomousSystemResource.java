package net.floodlightcontroller.tarn.web;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.floodlightcontroller.tarn.AutonomousSystem;
import net.floodlightcontroller.tarn.IRandomizerService;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 7/31/17.
 */
public class AutonomousSystemResource extends ServerResource {

    @Get
    public Object getAutonomousSystem() {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());

        Integer asNumber = Integer.valueOf(getRequestAttributes().get("as-number").toString());
        Optional<AutonomousSystem> as = randomizerService.getAutonomousSystem(asNumber);
        return as.isPresent() ? as.get() : Collections.singletonMap("ERROR", "AS " + asNumber + " not found");
    }

    @Put
    @Post
    public Object addPrefixToPool(String json) throws IOException {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());

        Integer asNumber = Integer.valueOf(getRequestAttributes().get("as-number").toString());
        Optional<AutonomousSystem> as = randomizerService.getAutonomousSystem(asNumber);
        if (!as.isPresent()) {
            return Collections.singletonMap("ERROR", "AS " + asNumber + " not found");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode prefixNode = objectMapper.readTree(json).get("prefix");
        if (prefixNode == null) {
            return Collections.singletonMap("ERROR", "'prefix' node expected but not found");
        }

        IPv4AddressWithMask prefix;
        try {
           prefix = IPv4AddressWithMask.of(prefixNode.asText());
        } catch (IllegalArgumentException e) {
            return Collections.singletonMap("ERROR", e.getMessage());
        }

        as.get().addPrefix(prefix);
        return Collections.singletonMap("SUCCESS", "Prefix " + prefix + " added to AS " + asNumber + " prefix pool");
    }
}
