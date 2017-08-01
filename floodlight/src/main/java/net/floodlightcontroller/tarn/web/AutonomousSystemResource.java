package net.floodlightcontroller.tarn.web;

import java.util.Collections;
import java.util.Optional;

import org.restlet.resource.Get;

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
}
