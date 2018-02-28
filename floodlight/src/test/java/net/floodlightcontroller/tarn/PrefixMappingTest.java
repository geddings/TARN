package net.floodlightcontroller.tarn;

import net.floodlightcontroller.tarn.utils.IPUtils;
import org.junit.Test;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IPv6AddressWithMask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by @geddings on 2/28/18.
 */
public class PrefixMappingTest {
    
    @Test
    public void testCreateIPv4PrefixMapping() {
        IPv4Address internalIp = IPv4Address.of("10.0.0.1");
        IPv4AddressWithMask externalPrefix = IPv4AddressWithMask.of("20.0.0.0/24");
        
        PrefixMapping mapping = new PrefixMapping(internalIp.toString(), externalPrefix.toString());
        
        assertEquals(internalIp, mapping.getInternalIp());
        assertEquals(externalPrefix, mapping.getCurrentPrefix());
        assertTrue(mapping.isInternalIp(internalIp));
        assertTrue(mapping.isExternalIp(IPUtils.getRandomAddressFrom(externalPrefix)));
    }

    @Test
    public void testCreateIPv6PrefixMapping() {
        IPv6Address internalIp = IPv6Address.of("fe80::1c41:baff:fe28:963");
        IPv6AddressWithMask externalPrefix = IPv6AddressWithMask.of("2001:db8:1234::/48");

        PrefixMapping mapping = new PrefixMapping(internalIp.toString(), externalPrefix.toString());

        assertEquals(internalIp, mapping.getInternalIp());
        assertEquals(externalPrefix, mapping.getCurrentPrefix());
        assertTrue(mapping.isInternalIp(internalIp));
        assertTrue(mapping.isExternalIp(IPUtils.getRandomAddressFrom(externalPrefix)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePrefixMappingThrowsException() {
        new PrefixMapping("Not a real IP", "Not a real prefix");
    }
}
