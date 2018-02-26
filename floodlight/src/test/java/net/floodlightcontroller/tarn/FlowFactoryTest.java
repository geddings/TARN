package net.floodlightcontroller.tarn;

import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.tarn.internal.FlowFactoryImpl;
import net.floodlightcontroller.tarn.types.TarnIPv4Session;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by geddingsbarrineau on 1/27/17.
 */
public class FlowFactoryTest {
    private FlowFactory flowFactory;

    @Before
    public void setUp() throws Exception {
        flowFactory = new FlowFactoryImpl();
    }

    @Test
    public void testOneWayFlowRules() {
        IPv4 iPv4 = new IPv4().setProtocol(IpProtocol.ICMP)
                .setSourceAddress("10.0.0.1")
                .setDestinationAddress("50.0.0.1");
        PrefixMapping dstMapping = new PrefixMapping("50.0.0.1", "80.0.0.0/24");
        TarnIPv4Session session = new TarnIPv4Session(iPv4, null, dstMapping, OFPort.of(1), OFPort.of(2));

        List<OFMessage> flows = flowFactory.buildFlows(session);

        for (OFMessage flow : flows) {
            OFFlowAdd add = (OFFlowAdd) flow;
            if (add.getCookie().equals(FlowFactory.OUTGOING_FLOW_COOKIE)) {
                assertEquals(session.getExternalSrcIp(), add.getMatch().get(MatchField.IPV4_SRC));
                assertEquals(session.getExternalDstIp(), add.getMatch().get(MatchField.IPV4_DST));
            } else {
                assertEquals(session.getInternalSrcIp(), add.getMatch().get(MatchField.IPV4_SRC));
                assertEquals(session.getInternalDstIp(), add.getMatch().get(MatchField.IPV4_DST));
            }
        }
    }

    @Test
    public void testActions() {
        IPv4 iPv4 = new IPv4().setProtocol(IpProtocol.ICMP)
                .setSourceAddress("10.0.0.1")
                .setDestinationAddress("50.0.0.1");
        PrefixMapping srcMapping = new PrefixMapping("10.0.0.1", "80.0.0.0/24");
        TarnIPv4Session session = new TarnIPv4Session(iPv4, srcMapping, null, OFPort.of(1), OFPort.of(2));

        List<OFAction> actions = ((FlowFactoryImpl) flowFactory).buildOutgoingActions(session);

        System.out.println(actions);
    }
}
