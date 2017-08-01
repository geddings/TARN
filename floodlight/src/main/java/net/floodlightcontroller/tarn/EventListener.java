package net.floodlightcontroller.tarn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 6/30/17.
 */
public class EventListener {
    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    private static int eventsHandled;

    @Subscribe
    public void stringEvent(String event) {
        eventsHandled++;
        log.info("String event: {}", event);
    }

    @Subscribe
    public void prefixChangeEvent(PrefixChangeEvent event) {
        log.info("{}", event);
        FlowFactory.insertPrefixRewriteFlows(event.getAs());
        eventsHandled++;
    }
}
