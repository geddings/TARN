package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import net.floodlightcontroller.tarn.TarnService;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import java.io.IOException;

/**
 * Created by @geddings on 2/15/18.
 */
public class ConfigResource extends ServerResource {

    @Get
    public ImmutableMap<String, Object> getTarnConfiguration() {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());
        ImmutableMap.Builder<String, Object> config = new ImmutableMap.Builder<>();
        config.put("enable", tarnService.isEnabled());
        return config.build();
    }

    @Put
    public void configureTarn(String json) throws IOException {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());

        JsonNode jsonNode = new ObjectMapper().readTree(json);

        JsonNode enableNode = jsonNode.get("enable");
        if (enableNode != null) {
            tarnService.setEnable(enableNode.asBoolean());
        }
    }
}
