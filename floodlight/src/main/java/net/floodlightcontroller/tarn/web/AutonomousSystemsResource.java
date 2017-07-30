package net.floodlightcontroller.tarn.web;

import org.restlet.resource.Get;

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
}
