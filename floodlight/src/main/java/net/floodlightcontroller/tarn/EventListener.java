package net.floodlightcontroller.tarn;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 6/30/17.
 */
public class EventListener {
    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    private final TarnService randomizer;

    /* Event statistics */
    private int eventsHandled;
    private int sessions;

    public EventListener(TarnService randomizer) {
        this.randomizer = randomizer;
    }

    @Subscribe
    public void stringEvent(String event) {
        log.info("String event: {}", event);
        eventsHandled++;
    }

}
