package net.floodlightcontroller.tarn.web;

import java.io.IOException;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.floodlightcontroller.tarn.AutonomousSystem;
import net.floodlightcontroller.tarn.IRandomizerService;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 7/30/17.
 */
public class AutonomousSystemsResource extends ServerResource {

    @Get
    public Object getAutonomousSystems() {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        return randomizerService.getAutonomousSystems();
    }

    @Put
    @Post
    public void setAutonomousSystems(String json) throws IOException {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        AutonomousSystem as = new ObjectMapper()
                .reader(AutonomousSystem.class)
                .readValue(json);
        randomizerService.addAutonomousSystem(as);
    }
}
