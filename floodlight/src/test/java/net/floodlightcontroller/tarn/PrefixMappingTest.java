package net.floodlightcontroller.tarn;

import org.junit.Test;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;

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
        assertTrue(mapping.isExternalIp(IPv4Address.of("20.0.0.1")));
    }
}
