package net.floodlightcontroller.tarn.web;

import org.restlet.Context;
import org.restlet.routing.Router;

import net.floodlightcontroller.restserver.RestletRoutable;

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
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/tarn";
    }
}
