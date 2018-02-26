package net.floodlightcontroller.tarn;

import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.tarn.types.TarnIPv4Session;
import org.junit.Test;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;

import static org.junit.Assert.assertEquals;

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

        assertEquals(internalPort, session.getInternalPort());
        assertEquals(externalPort, session.getExternalPort());
    }
}
