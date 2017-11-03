package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;

import java.util.Random;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 8/18/17.
 */
public class IPGenerator {

    private static Random rng = new Random();

    public static IPv4Address getRandomAddressFrom(IPv4AddressWithMask prefix) {
        return IPv4Address.of(rng.nextInt())
                .and(prefix.getMask().not())
                .or(prefix.getValue());
    }

}
