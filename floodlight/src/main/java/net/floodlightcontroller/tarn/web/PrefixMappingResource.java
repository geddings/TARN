package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.floodlightcontroller.tarn.PrefixMapping;
import net.floodlightcontroller.tarn.TarnService;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

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
        PrefixMapping mapping = new ObjectMapper()
                .reader(PrefixMapping.class)
                .readValue(json);
        tarnService.addPrefixMapping(mapping);
    }
}
