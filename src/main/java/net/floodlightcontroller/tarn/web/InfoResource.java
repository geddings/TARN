package net.floodlightcontroller.tarn.web;

import org.restlet.resource.Get;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by geddingsbarrineau on 10/29/16.
 *
 */
public class InfoResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(InfoResource.class);

//    public static class InfoJsonSerializerWrapper {
//        private final String prefix;
//        private final List<RandomizedHost> randomizedHosts;
//
//        public InfoJsonSerializerWrapper(String prefix, List<RandomizedHost> randomizedHosts) {
//            this.prefix = prefix;
//            this.randomizedHosts = randomizedHosts;
//        }
//    }

//    @Get
//    public Object getEAGERInfo() {
//        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
//        Map<String, String> ret = new HashMap<>();
//        // FIXME: This is broken with the new prefixes implementation
//        ret.put("current-prefix", randomizerService.getCurrentPrefix().toString());
//        return ret;
//    }

    @Get
    public Object getAllRoutes() {
        Router router = (Router)this.getApplication().getInboundRoot();
        for (Route route : router.getRoutes()) {
            log.info("{}", route);
        }
        return router.getRoutes();
    }
}
