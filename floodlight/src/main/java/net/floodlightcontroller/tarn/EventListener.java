package net.floodlightcontroller.tarn;

import com.google.common.eventbus.Subscribe;
import net.floodlightcontroller.tarn.events.HostChangeEvent;
import net.floodlightcontroller.tarn.events.PrefixChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 6/30/17.
 */
public class EventListener {
    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    private final RandomizerService randomizer;

    /* Event statistics */
    private int eventsHandled;
    private int sessions;

    EventListener(RandomizerService randomizer) {
        this.randomizer = randomizer;
    }

    @Subscribe
    public void stringEvent(String event) {
        log.info("String event: {}", event);
        eventsHandled++;
    }

    @Subscribe
    public void prefixChangeEvent(PrefixChangeEvent event) {
        log.info("{}", event);
        FlowFactoryImpl.insertASRewriteFlows(event.getAS());
        eventsHandled++;
    }

    @Subscribe
    public void hostChangeEvent(HostChangeEvent event) {
        log.info("{}", event);
        Host host = event.getHost();
        randomizer.sendGratuitiousArp(host);
        Optional<AutonomousSystem> as = randomizer.getAutonomousSystem(host.getMemberAS());
        if (as.isPresent()) {
            FlowFactoryImpl.insertHostRewriteFlows(event.getHost(), as.get());
        } else {
            log.error("Host {} member AS {} not found.", host.getInternalAddress(), host.getMemberAS());
        }
    }
}
