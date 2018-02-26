package net.floodlightcontroller.tarn;

import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.tarn.internal.FlowFactoryImpl;
import net.floodlightcontroller.tarn.types.TarnIPv4Session;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Test
    public void testTwoWayRandomizationFlows() {
        IPv4 iPv4 = new IPv4().setProtocol(IpProtocol.ICMP)
                .setSourceAddress("10.0.0.1")
                .setDestinationAddress("50.0.0.1");
        PrefixMapping srcMapping = new PrefixMapping("10.0.0.1", "80.0.0.0/24");
        PrefixMapping dstMapping = new PrefixMapping("50.0.0.1", "90.0.0.0/24");
        TarnIPv4Session session = new TarnIPv4Session(iPv4, srcMapping, dstMapping, OFPort.of(1), OFPort.of(2));

        List<OFMessage> actualFlows = flowFactory.buildFlows(session);
        List<OFMessage> expectedFlows = getTwoWayFlows("10.0.0.1", "50.0.0.1", session.getExternalSrcIp().toString
                (), session
                .getExternalDstIp().toString());

        assertEquals(expectedFlows, actualFlows);

    }

    public List<OFMessage> getTwoWayFlows(String internalSrcIp, String internalDstIp, String externalSrcIp, String
                                    externalDstIp) {
        OFFactory factory = OFFactories.getFactory(OFVersion.OF_15);
        List<OFMessage> expected = new ArrayList<>();

                /* IPv4 flow */
        expected.add(factory.buildFlowAdd()
                .setXid(1)
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(5)
                .setPriority(100)
                .setCookie(FlowFactory.OUTGOING_FLOW_COOKIE)
                .setMatch(factory.buildMatch()
                        .setExact(MatchField.IN_PORT, OFPort.of(1))
                        .setExact(MatchField.ETH_TYPE, EthType.IPv4)
                        .setExact(MatchField.IPV4_SRC, IPv4Address.of(internalSrcIp))
                        .setExact(MatchField.IPV4_DST, IPv4Address.of(internalDstIp))
                        .build())
                .setActions(Arrays.asList(
                        factory.actions().buildSetField()
                                .setField(factory.oxms().buildIpv4Src()
                                        .setValue(IPv4Address.of(externalDstIp))
                                        .build())
                                .build(),
                        factory.actions().buildSetField()
                                .setField(factory.oxms().buildIpv4Dst()
                                        .setValue(IPv4Address.of(externalSrcIp))
                                        .build())
                                .build(),
                        factory.actions().buildOutput()
                                .setMaxLen(0xFFffFFff)
                                .setPort(OFPort.of(2))
                                .build()
                )).build());

        expected.add(factory.buildFlowAdd()
                .setXid(2)
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(5)
                .setPriority(100)
                .setCookie(FlowFactory.INCOMING_FLOW_COOKIE)
                .setMatch(factory.buildMatch()
                        .setExact(MatchField.IN_PORT, OFPort.of(2))
                        .setExact(MatchField.ETH_TYPE, EthType.IPv4)
                        .setExact(MatchField.IPV4_SRC, IPv4Address.of(externalSrcIp))
                        .setExact(MatchField.IPV4_DST, IPv4Address.of(externalDstIp))
                        .build())
                .setActions(Arrays.asList(
                        factory.actions().buildSetField()
                                .setField(factory.oxms().buildIpv4Src()
                                        .setValue(IPv4Address.of(internalDstIp))
                                        .build())
                                .build(),
                        factory.actions().buildSetField()
                                .setField(factory.oxms().buildIpv4Dst()
                                        .setValue(IPv4Address.of(internalSrcIp))
                                        .build())
                                .build(),
                        factory.actions().buildOutput()
                                .setMaxLen(0xFFffFFff)
                                .setPort(OFPort.of(1))
                                .build()
                )).build());

        return expected;
    }
}
