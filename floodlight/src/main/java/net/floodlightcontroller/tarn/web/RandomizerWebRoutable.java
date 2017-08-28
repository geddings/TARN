package net.floodlightcontroller.tarn.web;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.routing.Router;

/**
 * Created by geddingsbarrineau on 9/19/16.
 *
 *
 */
public class RandomizerWebRoutable implements RestletRoutable {

    @Override
    public Router getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/config/json", ConfigResource.class);
        router.attach("/info/json", InfoResource.class);
        router.attach("/as/json", AutonomousSystemsResource.class);
        router.attach("/as/{as-number}/json", AutonomousSystemResource.class);
        router.attach("/host/json", HostsResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/tarn";
    }
}
