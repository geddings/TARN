package net.floodlightcontroller.tarn;

import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.IPv6;
import net.floodlightcontroller.tarn.utils.IPUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.types.IPAddress;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IPv6AddressWithMask;
import org.projectfloodlight.openflow.types.MacAddress;

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
    public void getMapping() {
        IPAddress address = IPUtils.getRandomAddressFrom(IPv6AddressWithMask.of("2001:db8:1234::/48"));

        Assert.assertTrue(IPUtils.isIpv6Address("fe80::1c41:baff:fe28:963"));
        IPv6Address ipv6addr = IPv6Address.of("fe80::1c41:baff:fe28:963");
        System.out.println(ipv6addr);
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
    public void getAssociatedMappingV6() throws Exception {
        IPv6Address internal = IPv6Address.of("2001:db8:1111::1");
        IPv6AddressWithMask externalPrefix = IPv6AddressWithMask.of("2001:db8:2222::/48");
        IPv6Address external = IPv6Address.of("2001:db8:2222::1");
        PrefixMapping mapping = new PrefixMapping(internal.toString(false, false), externalPrefix.toString());

        mappingHandler.addMapping(mapping);

        Assert.assertTrue(mappingHandler.getAssociatedMapping(internal).isPresent());
        Assert.assertEquals(mapping, mappingHandler.getAssociatedMapping(internal).get());

        Assert.assertTrue(mappingHandler.getAssociatedMapping(external).isPresent());
        Assert.assertEquals(mapping, mappingHandler.getAssociatedMapping(external).get());

        Assert.assertTrue(mappingHandler.getAssociatedMapping(IPv6Address.of(externalPrefix, MacAddress.of(77))).isPresent());
        Assert.assertEquals(mapping, mappingHandler.getAssociatedMapping(IPv6Address.of(externalPrefix, MacAddress.of(77))).get());
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
    public void isTarnDeviceV6() throws Exception {
        IPv6Address internal = IPv6Address.of("2001:db8:1111::1");
        IPv6Address host = IPv6Address.of("2001:db8:1234::1");
        IPv6AddressWithMask externalPrefix = IPv6AddressWithMask.of("2001:db8:2222::/48");
        IPv6Address external = IPv6Address.of("2001:db8:2222::1");
        PrefixMapping mapping = new PrefixMapping(internal.toString(false, false), externalPrefix.toString());

        mappingHandler.addMapping(mapping);

        IPv6 outboundIPv6 = new IPv6().setSourceAddress(internal).setDestinationAddress(host);
        Assert.assertTrue(mappingHandler.isTarnDevice(outboundIPv6));

        IPv6 inboundIPv6 = new IPv6().setSourceAddress(host).setDestinationAddress(external);
        Assert.assertTrue(mappingHandler.isTarnDevice(inboundIPv6));
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