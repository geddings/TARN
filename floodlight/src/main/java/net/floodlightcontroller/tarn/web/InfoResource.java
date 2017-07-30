package net.floodlightcontroller.tarn.web;

import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import net.floodlightcontroller.tarn.IRandomizerService;

/**
 * Created by geddingsbarrineau on 10/29/16.
 *
 */
public class InfoResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(InfoResource.class);

    @Get
    public ImmutableMap<String, Object> getTARNInfo() {
        IRandomizerService tarn = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        ImmutableMap.Builder<String, Object> info = new ImmutableMap.Builder<>();
        info.put("as-count", tarn.getAutonomousSystems().size());
        return info.build();
    }
}
