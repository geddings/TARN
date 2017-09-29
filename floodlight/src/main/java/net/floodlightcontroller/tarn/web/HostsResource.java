package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.floodlightcontroller.tarn.Host;
import net.floodlightcontroller.tarn.IRandomizerService;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import java.io.IOException;

/**
 * Created by geddingsbarrineau on 8/28/17.
 */
public class HostsResource extends ServerResource {

    @Get
    public Object getHosts() {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        return randomizerService.getHosts();
    }

    @Put
    @Post
    public void setHosts(String json) throws IOException {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        Host host = new ObjectMapper()
                .reader(Host.class)
                .readValue(json);
        randomizerService.addHost(host);
    }
}
