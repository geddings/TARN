package net.floodlightcontroller.tarn.utils;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.projectfloodlight.openflow.types.IPAddress;
import org.projectfloodlight.openflow.types.IPAddressWithMask;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IPv6AddressWithMask;
import org.projectfloodlight.openflow.types.MacAddress;

/**
 * Utility class for generating random IP addresses.
 *
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 8/18/17.
 */
public class IPUtils {

    private static Pattern VALID_IPV4_PATTERN = null;
    private static Pattern VALID_IPV6_PATTERN = null;
    private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
    private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

    static {
        try {
            VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
            VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            //logger.severe("Unable to compile pattern", e);
        }
    }

    public static boolean isIpv4Address(String ipAddress) {
        Matcher m = VALID_IPV4_PATTERN.matcher(ipAddress);
        return m.matches();
    }

    public static boolean isIpv6Address(String ipAddress) {
        Matcher m = VALID_IPV6_PATTERN.matcher(ipAddress);
        return m.matches();
    }

    private static Random rng = new Random();

    private IPUtils() {
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
