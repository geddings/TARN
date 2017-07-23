package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.test.FloodlightTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * Created by geddingsbarrineau on 1/27/17.
 */
public class FlowFactoryTest extends FloodlightTestCase {
    
//    final IPv4Address HOST_IP = IPv4Address.of("10.0.0.1");
//    final IPv4Address RANDOMIZEDHOST_IP = IPv4Address.of("20.0.0.1");
//    final IPv4Address RANDOMIZEDHOST_RANDOMIP = IPv4Address.of("30.0.0.1");
//
//
//    IFlowFactory ff;
//    IHost host;
//    IHost randomizedHost;
//    IOFSwitch sw;
//    IOFSwitchService switchService;
//    Connection connection;
//    OFFactory factory;
//
//    private IOFSwitch createMockSwitch(DatapathId dpid) {
//        IOFSwitch mockSwitch = createNiceMock(IOFSwitch.class);
//        expect(mockSwitch.getId()).andReturn(dpid).anyTimes();
//        return mockSwitch;
//    }
//
//    private IOFSwitchService createMockSwitchService() {
//        return createNiceMock(IOFSwitchService.class);
//    }
//
//    @Before
//    public void SetUp() throws Exception {
//        super.setUp();
//
//        /* Initialize hosts */
//        host = createNiceMock(IHost.class);
//        expect(host.getAddress(IHost.AddressType.INTERNAL)).andReturn(HOST_IP).anyTimes();
//        replay(host);
//
//        randomizedHost = createNiceMock(ITARNHost.class);
//        expect(randomizedHost.getAddress(ITARNHost.AddressType.INTERNAL)).andReturn(RANDOMIZEDHOST_IP).anyTimes();
//        expect(randomizedHost.getAddress(ITARNHost.AddressType.EXTERNAL)).andReturn(RANDOMIZEDHOST_RANDOMIP).anyTimes();
//        replay(randomizedHost);
//
//        /* Initialize switch */
//        DatapathId dpid = DatapathId.of(1L);
//        sw = createMockSwitch(dpid);
//        replay(sw);
//
//        /* Initialize connection */
//        switchService = createMockSwitchService();
//        expect(switchService.getActiveSwitch(dpid)).andReturn(sw).anyTimes();
//        replay(switchService);
//
//        connection = createNiceMock(Connection.class);
//        expect(connection.getSource()).andReturn(host).anyTimes();
//        expect(connection.getDestination()).andReturn(randomizedHost).anyTimes();
//        expect(connection.getDirection()).andReturn(Direction.OUTGOING).anyTimes();
//        replay(connection);
//
//        /* Initialize flow factory */
//        ff = new FlowFactory(connection);
//        FlowFactory.setLanport(OFPort.of(1));
//        FlowFactory.setWanport(OFPort.of(2));
//
//        factory = OFFactories.getFactory(OFVersion.OF_13);
//    }
//
//    @Test
//    public void testGetFlows() {
//        OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);
//        List<OFFlowMod> expected = new ArrayList<>();
//
//        /* IPv4 flow */
//        expected.add(factory.buildFlowAdd()
//                .setBufferId(OFBufferId.NO_BUFFER)
//                .setHardTimeout(30)
//                .setIdleTimeout(30)
//                .setPriority(32768)
//                .setMatch(factory.buildMatch()
//                        .setExact(MatchField.ETH_TYPE, EthType.IPv4)
//                        .setExact(MatchField.IPV4_SRC, HOST_IP)
//                        .setExact(MatchField.IPV4_DST, RANDOMIZEDHOST_IP)
//                        .build())
//                .setActions(Arrays.asList(
//                        factory.actions().buildOutput()
//                            .setMaxLen(0xFFffFFff)
//                            .setPort(OFPort.of(2))
//                            .build(),
//                        factory.actions().buildSetField()
//                            .setField(factory.oxms().buildIpv4Dst()
//                                        .setValue(RANDOMIZEDHOST_RANDOMIP)
//                                        .build())
//                            .build()
//                )).build());
//
//        /* ARP flow */
//        expected.add(factory.buildFlowAdd()
//                .setBufferId(OFBufferId.NO_BUFFER)
//                .setHardTimeout(30)
//                .setIdleTimeout(30)
//                .setPriority(32768)
//                .setMatch(factory.buildMatch()
//                        .setExact(MatchField.ETH_TYPE, EthType.ARP)
//                        .setExact(MatchField.ARP_SPA, HOST_IP)
//                        .setExact(MatchField.ARP_TPA, RANDOMIZEDHOST_IP)
//                        .build())
//                .setActions(Arrays.asList(
//                        factory.actions().buildOutput()
//                                .setMaxLen(0xFFffFFff)
//                                .setPort(OFPort.of(2))
//                                .build(),
//                        factory.actions().buildSetField()
//                                .setField(factory.oxms().buildArpTpa()
//                                        .setValue(RANDOMIZEDHOST_RANDOMIP)
//                                        .build())
//                                .build()
//                )).build());
//
//        List<OFFlowMod> actual = ff.getFlows(OFFlowModCommand.ADD);
//
//        Assert.assertEquals(expected, actual);
//    }
//
//
//    List<OFFlowMod> getTestFlows() {
//        OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);
//        List<OFFlowMod> expected = new ArrayList<>();
//
//        int timeout = 30;
//        int priority = 32768;
//        /* IPv4 flow */
//        expected.add(factory.buildFlowAdd()
//                .setBufferId(OFBufferId.NO_BUFFER)
//                .setHardTimeout(timeout)
//                .setIdleTimeout(timeout)
//                .setPriority(priority)
//                .setMatch(factory.buildMatch()
//                        .setExact(MatchField.ETH_TYPE, EthType.IPv4)
//                        .setExact(MatchField.IPV4_SRC, HOST_IP)
//                        .setExact(MatchField.IPV4_DST, RANDOMIZEDHOST_IP)
//                        .build())
//                .setActions(Arrays.asList(
//                        factory.actions().buildOutput()
//                                .setMaxLen(0xFFffFFff)
//                                .setPort(OFPort.of(2))
//                                .build(),
//                        factory.actions().buildSetField()
//                                .setField(factory.oxms().buildIpv4Dst()
//                                        .setValue(RANDOMIZEDHOST_RANDOMIP)
//                                        .build())
//                                .build()
//                )).build());
//
//        /* ARP flow */
//        expected.add(factory.buildFlowAdd()
//                .setBufferId(OFBufferId.NO_BUFFER)
//                .setHardTimeout(timeout)
//                .setIdleTimeout(timeout)
//                .setPriority(priority)
//                .setMatch(factory.buildMatch()
//                        .setExact(MatchField.ETH_TYPE, EthType.ARP)
//                        .setExact(MatchField.ARP_SPA, HOST_IP)
//                        .setExact(MatchField.ARP_TPA, RANDOMIZEDHOST_IP)
//                        .build())
//                .setActions(Arrays.asList(
//                        factory.actions().buildOutput()
//                                .setMaxLen(0xFFffFFff)
//                                .setPort(OFPort.of(2))
//                                .build(),
//                        factory.actions().buildSetField()
//                                .setField(factory.oxms().buildArpTpa()
//                                        .setValue(RANDOMIZEDHOST_RANDOMIP)
//                                        .build())
//                                .build()
//                )).build());
//        return expected;
//    }
//

}
