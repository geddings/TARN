package net.floodlightcontroller.tarn.web;

import net.floodlightcontroller.tarn.IRandomizerService;
import net.floodlightcontroller.tarn.RandomizedHost;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by geddingsbarrineau on 10/29/16.
 *
 */
public class InfoResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(InfoResource.class);

    public static class InfoJsonSerializerWrapper {
        private final String prefix;
        private final List<RandomizedHost> randomizedHosts;

        public InfoJsonSerializerWrapper(String prefix, List<RandomizedHost> randomizedHosts) {
            this.prefix = prefix;
            this.randomizedHosts = randomizedHosts;
        }
    }

    @Get
    public Object getEAGERInfo() {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        Map<String, String> ret = new HashMap<>();
        // FIXME: This is broken with the new prefixes implementation
        ret.put("current-prefix", randomizerService.getCurrentPrefix().toString());
        return ret;
    }
}
