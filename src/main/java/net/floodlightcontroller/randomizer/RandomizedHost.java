package net.floodlightcontroller.randomizer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.randomizer.web.RandomizedHostSerializer;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by geddingsbarrineau on 8/31/16.
 * <p>
 * This is a RandomizedHost object for the EAGER project.
 */
@JsonSerialize(using = RandomizedHostSerializer.class)
public class RandomizedHost implements ITARNHost {
    private static Logger log = LoggerFactory.getLogger(RandomizedHost.class);

    /**
     * The marker is simply the address/string/number that will be used
     * by an application to communicate with a TARN host. This marker will
     * be used somewhere in the header of a transport packet – most likely
     * in the src/dst address. The marker is used to craft flows that match
     * on all traffic being sent to a specific TARN host. The marker will be
     * rewritten to the external address.
     */
    private IPv4Address marker;
    private IPv4Address randomizedAddress;

    private List<IPv4AddressWithMask> prefixes;
    private IPv4AddressWithMask prefix;
    private Random generator;

    public RandomizedHost(IPv4Address marker, List<IPv4AddressWithMask> prefixes) {
        this.marker = marker;
        this.prefixes = prefixes;
        generator = new Random();
        updatePrefix();
        update();
    }

    public RandomizedHost(IPv4Address marker) {
        this.marker = marker;
        prefixes = new ArrayList<>();
        generator = new Random();
        updatePrefix();
        update();
    }

    public void update() {
        generator.setSeed(LocalTime.now().toSecondOfDay() % marker.getInt());
        randomizedAddress = IPv4Address.of(generator.nextInt())
                .and(prefix.getMask().not())
                .or(prefix.getValue());
        log.debug("New external address: {}", randomizedAddress);
    }

    public void updatePrefix() {
        if (!prefixes.isEmpty()) {
            prefix = prefixes.get(LocalDateTime.now().getMinute() % prefixes.size());
        } else {
            prefix = IPv4AddressWithMask.NONE;
        }
    }

    @Override
    public IPv4Address getAddress(AddressType type) {
        if (type == AddressType.INTERNAL) {
            return marker;
        } else {
            return randomizedAddress;
        }
    }

    public IPv4Address getRandomizedAddress() {
        return randomizedAddress;
    }

    public IPv4AddressWithMask getPrefix() {
        return prefix;
    }

    public void addPrefix(IPv4AddressWithMask prefix) {
        prefixes.add(prefix);
    }

    public void removePrefix(IPv4AddressWithMask prefix) {
        prefixes.remove(prefix);
    }

    public List<IPv4AddressWithMask> getPrefixes() {
        return prefixes;
    }
    
    @Override
    public String toString() {
        return "RandomizedHost{" +
                "randomizedAddress=" + randomizedAddress +
                ", prefix=" + prefix +
                "} " + super.toString();
    }


}
