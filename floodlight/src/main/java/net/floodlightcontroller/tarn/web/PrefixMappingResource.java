package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.floodlightcontroller.tarn.PrefixMapping;
import net.floodlightcontroller.tarn.TarnService;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.io.IOException;

/**
 * Created by @geddings on 11/8/17.
 */
public class PrefixMappingResource extends ServerResource {

    @Get
    public Object getPrefixMappings() {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());
        return tarnService.getPrefixMappings();
    }

    @Put
    @Post
    public void addPrefixMapping(String json) throws IOException {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());

        if (json == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Must provide an internal-ip and external-prefix for a prefix mapping to be added.");
            return;
        }

        PrefixMapping mapping = new ObjectMapper()
                .reader(PrefixMapping.class)
                .readValue(json);
        tarnService.addPrefixMapping(mapping);
    }

    @Delete
    public void removePrefixMapping(String json) throws IOException {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());

        if (json == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Must provide an internal-ip in order for mapping to be removed.");
            return;
        }

        JsonNode jsonNode = new ObjectMapper().readTree(json);

        JsonNode internalNode = jsonNode.get("internal-ip");
        if (internalNode != null) {
            try {
                IPv4Address internalIp = IPv4Address.of(internalNode.asText());
                tarnService.removePrefixMapping(internalIp);
            } catch (IllegalArgumentException e) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please use a valid internal IP address.");
            }
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Must provide an internal-ip in order for mapping to be removed.");
        }
    }
}
