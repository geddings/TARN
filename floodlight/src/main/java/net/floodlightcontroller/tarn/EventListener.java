package net.floodlightcontroller.tarn;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 6/30/17.
 */
public class EventListener {
    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    private final IRandomizerService randomizer;

    /* Event statistics */
    private int eventsHandled;

    EventListener(IRandomizerService randomizer) {
        this.randomizer = randomizer;
    }

    @Subscribe
    public void stringEvent(String event) {
        eventsHandled++;
        log.info("String event: {}", event);
    }

    @Subscribe
    public void prefixChangeEvent(PrefixChangeEvent event) {
        log.info("{}", event);
        FlowFactory.insertASRewriteFlows(event.getAS());
        eventsHandled++;
    }

    @Subscribe
    public void hostChangeEvent(HostChangeEvent event) {
        log.info("{}", event);
        Host host = event.getHost();
        Optional<AutonomousSystem> as = randomizer.getAutonomousSystem(host.getMemberAS());
        if (as.isPresent()) {
            FlowFactory.insertHostRewriteFlows(event.getHost(), as.get());
        } else {
            log.error("Host {} member AS {} not found.", host.getInternalAddress(), host.getMemberAS());
        }
    }
}
