package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.floodlightcontroller.tarn.IRandomizerService;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by geddingsbarrineau on 9/21/16.
 */
public class ConfigResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(ConfigResource.class);
    
    @Put
    @Post
    public Object configure(String json) throws IOException {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        
        String message = "";
        
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode lanPortNode = objectMapper.readTree(json).get("lanport");
        if (lanPortNode != null) {
            randomizerService.setLanPort(lanPortNode.asInt());
            message = message + "LAN port set to " + lanPortNode.asInt() + ". ";
        }
        JsonNode wanPortNode = objectMapper.readTree(json).get("wanport");
        if (wanPortNode != null) {
            randomizerService.setWanPort(wanPortNode.asInt());
            message = message + "WAN port set to " + wanPortNode.asInt() + ". ";
        }
        
        return Collections.singletonMap("STATUS", message);
    }
}
