package net.floodlightcontroller.tarn;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;

import net.floodlightcontroller.packet.IPv4;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/3/17.
 */
public class PrefixMappingHandlerTest {

    private PrefixMappingHandler mappingHandler;

    @Before
    public void setUp() throws Exception {
        mappingHandler = new PrefixMappingHandler();
    }

    @Test
    public void getMapping() throws Exception {
    }

    @Test
    public void getAssociatedMapping() throws Exception {
        PrefixMapping mapping = new PrefixMapping("10.0.0.1", "50.0.0.0/24");

        mappingHandler.addMapping(mapping);

        Assert.assertTrue(mappingHandler.getAssociatedMapping(IPv4Address.of("10.0.0.1")).isPresent());
        Assert.assertEquals(mapping, mappingHandler.getAssociatedMapping(IPv4Address.of("10.0.0.1")).get());

        Assert.assertTrue(mappingHandler.getAssociatedMapping(IPv4Address.of("50.0.0.1")).isPresent());
        Assert.assertEquals(mapping, mappingHandler.getAssociatedMapping(IPv4Address.of("50.0.0.1")).get());

        Assert.assertTrue(mappingHandler.getAssociatedMapping(IPv4Address.of("50.0.0.77")).isPresent());
        Assert.assertEquals(mapping, mappingHandler.getAssociatedMapping(IPv4Address.of("50.0.0.77")).get());
    }

    @Test
    public void isTarnDevice() throws Exception {
        IPv4Address internal = IPv4Address.of("10.0.0.1");
        IPv4AddressWithMask externalPrefix = IPv4AddressWithMask.of("50.0.0.0/24");
        IPv4Address external = IPv4Address.of("50.0.0.77");

        PrefixMapping mapping = new PrefixMapping(internal.toString(), externalPrefix.toString());
        mappingHandler.addMapping(mapping);

        IPv4 outboundIPv4 = new IPv4().setSourceAddress(internal).setDestinationAddress("20.0.0.1");
        Assert.assertTrue(mappingHandler.isTarnDevice(outboundIPv4));

        IPv4 inboundIPv4 = new IPv4().setSourceAddress("20.0.0.1").setDestinationAddress(external);
        Assert.assertTrue(mappingHandler.isTarnDevice(inboundIPv4));
    }

    @Test
    public void testIsInternalIp() throws Exception {
        mappingHandler.addMapping(new PrefixMapping("10.0.0.1", "50.0.0.0/24"));

        Assert.assertTrue(mappingHandler.isInternalIp(IPv4Address.of("10.0.0.1")));
        Assert.assertFalse(mappingHandler.isInternalIp(IPv4Address.of("50.0.0.1")));
    }

    @Test
    public void testIsExternalIp() throws Exception {
        mappingHandler.addMapping(new PrefixMapping("10.0.0.1", "50.0.0.0/24"));

        Assert.assertFalse(mappingHandler.isExternalIp(IPv4Address.of("10.0.0.1")));
        Assert.assertTrue(mappingHandler.isExternalIp(IPv4Address.of("50.0.0.1")));
    }

    @Test
    public void testContainsInternalIp() throws Exception {
        mappingHandler.addMapping(new PrefixMapping("10.0.0.1", "50.0.0.0/24"));

        IPv4 iPv4 = new IPv4().setSourceAddress("10.0.0.1")
                .setDestinationAddress("20.0.0.1");

        Assert.assertTrue(mappingHandler.containsInternalIp(iPv4));
    }

    @Test
    public void testContainsExternalIp() throws Exception {
        mappingHandler.addMapping(new PrefixMapping("10.0.0.1", "50.0.0.0/24"));

        IPv4 iPv4 = new IPv4().setSourceAddress("20.0.0.1")
                .setDestinationAddress("50.0.0.1");

        Assert.assertTrue(mappingHandler.containsExternalIp(iPv4));
    }

}