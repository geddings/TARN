package net.floodlightcontroller.tarn.web;

import com.google.common.collect.ImmutableMap;
import net.floodlightcontroller.tarn.TarnService;
import net.floodlightcontroller.tarn.TarnSession;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by geddingsbarrineau on 10/29/16.
 *
 */
public class InfoResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(InfoResource.class);

    @Get
    public ImmutableMap<String, Object> getTarnInfo() {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());
        ImmutableMap.Builder<String, Object> info = new ImmutableMap.Builder<>();
        int i = 0;
        for (TarnSession session : tarnService.getSessions()) {
            info.put("session-" + i, session);
            i++;
        }
        return info.build();
    }
}
