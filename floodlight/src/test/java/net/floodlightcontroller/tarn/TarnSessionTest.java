package net.floodlightcontroller.tarn;

import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.IPv6;
import net.floodlightcontroller.tarn.types.TarnIPv4Session;
import net.floodlightcontroller.tarn.types.TarnIPv6Session;
import net.floodlightcontroller.tarn.utils.IPUtils;
import org.junit.Test;
import org.projectfloodlight.openflow.types.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/26/18.
 */
public class TarnSessionTest {

    @Test
    public void testTarnIPv4SessionCreatedCorrectly() {
        IPv4 outgoingIpv4 = new IPv4().setProtocol(IpProtocol.ICMP)
                .setSourceAddress("10.0.0.1")
                .setDestinationAddress("50.0.0.1");
        PrefixMapping srcMapping = new PrefixMapping("10.0.0.1", "80.0.0.0/24");
        OFPort internalPort = OFPort.of(1);
        OFPort externalPort = OFPort.of(2);

        TarnIPv4Session session = new TarnIPv4Session(outgoingIpv4, srcMapping, null, internalPort, externalPort);

        assertEquals(IPv4Address.of("10.0.0.1"), session.getInternalSrcIp());
        assertEquals(IPv4Address.of("50.0.0.1"), session.getInternalDstIp());
        assertEquals(IPv4Address.of("50.0.0.1"), session.getExternalSrcIp());
        assertTrue(IPv4AddressWithMask.of("80.0.0.0/24").contains(session.getExternalDstIp()));
        assertEquals(internalPort, session.getInternalPort());
        assertEquals(externalPort, session.getExternalPort());
    }

    @Test
    public void testTarnIPv6SessionCreatedCorrectly() {
        IPv6AddressWithMask docPrefix = IPv6AddressWithMask.of("2001:db8::/32");
        IPv6Address srcIp = (IPv6Address) IPUtils.getRandomAddressFrom(docPrefix);
        IPv6Address dstIp = (IPv6Address) IPUtils.getRandomAddressFrom(docPrefix);

        IPv6Address internalIp = srcIp;
        IPv6AddressWithMask externalPrefix = IPv6AddressWithMask.of("2001:db8:1234::/48");
        IPv6 outgoingIpv6 = new IPv6().setNextHeader(IpProtocol.ICMP)
                .setSourceAddress(srcIp)
                .setDestinationAddress(dstIp);
        PrefixMapping srcMapping = new PrefixMapping(internalIp.toString(), externalPrefix.toString());
        OFPort internalPort = OFPort.of(1);
        OFPort externalPort = OFPort.of(2);

        TarnIPv6Session session = new TarnIPv6Session(outgoingIpv6, srcMapping, null, internalPort, externalPort);

        assertEquals(srcIp, session.getInternalSrcIp());
        assertEquals(dstIp, session.getInternalDstIp());
        assertEquals(dstIp, session.getExternalSrcIp());
        assertTrue(externalPrefix.contains(session.getExternalDstIp()));
        assertEquals(internalPort, session.getInternalPort());
        assertEquals(externalPort, session.getExternalPort());
    }
}
