package net.floodlightcontroller.tarn.web;

import net.floodlightcontroller.restserver.RestletRoutable;
import org.restlet.Context;
import org.restlet.routing.Router;

/**
 * Created by geddingsbarrineau on 9/19/16.
 *
 *
 */
public class TarnWebRoutable implements RestletRoutable {

    @Override
    public Router getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/info", InfoResource.class);
        router.attach("/mapping", MappingsResource.class);
        router.attach("/mapping/{internal-ip}", MappingResource.class);
        router.attach("/config", ConfigResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/tarn";
    }
}
