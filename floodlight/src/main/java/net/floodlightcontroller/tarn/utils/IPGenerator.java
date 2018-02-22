package net.floodlightcontroller.tarn.utils;

import org.projectfloodlight.openflow.types.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Random;

/**
 * Utility class for generating random IP addresses.
 *
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 8/18/17.
 */
public class IPGenerator {

    private static Random rng = new Random();

    private IPGenerator() {
        throw new IllegalStateException("Utility class");
    }

    public static IPAddress getRandomAddressFrom(IPAddressWithMask prefix) {
        if (prefix instanceof IPv4AddressWithMask) {
            return getRandomAddressFrom((IPv4AddressWithMask) prefix);
        } else {
            return getRandomAddressFrom((IPv6AddressWithMask) prefix);
        }
    }
    
    /**
     * Given a prefix, generates and returns and returns a random IP address within the prefix.
     *
     * @param prefix the prefix in which the generated address will be contained
     * @return a random IP address within the given prefix
     */
    private static IPv4Address getRandomAddressFrom(IPv4AddressWithMask prefix) {
        return IPv4Address.of(rng.nextInt())
                .and(prefix.getMask().not())
                .or(prefix.getValue());
    }

    private static IPv6Address getRandomAddressFrom(IPv6AddressWithMask prefix) {
        return IPv6Address.of(prefix, MacAddress.of(rng.nextInt()));
    }

}
