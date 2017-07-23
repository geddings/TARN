package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.test.MockThreadPoolService;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.debugcounter.MockDebugCounterService;
import net.floodlightcontroller.packet.*;
import net.floodlightcontroller.test.FloodlightTestCase;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketInReason;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.easymock.EasyMock.createMock;

/**
 * Created by geddingsbarrineau on 9/23/16.
 *
 *
 */
public class RandomizerTest extends FloodlightTestCase {

    protected static Logger log = LoggerFactory.getLogger(RandomizerTest.class);
    Randomizer randomizer;
    FloodlightModuleContext fmc;
    protected OFPacketIn packetIn;
    protected IPacket testPacket;
    protected byte[] testPacketSerialized;
    protected IOFSwitch mockSwitch;

//    @Before
//    public void SetUp() throws Exception {
//        super.setUp();
//        fmc = new FloodlightModuleContext();
//
//        fmc.addService(IFloodlightProviderService.class, getMockFloodlightProvider());
//        fmc.addService(IDebugCounterService.class, new MockDebugCounterService());
//        MockThreadPoolService tp = new MockThreadPoolService();
//        fmc.addService(IThreadPoolService.class, tp);
//        randomizer = new Randomizer();
//        fmc.addConfigParam(randomizer,"lanport", "1");
//        fmc.addConfigParam(randomizer,"wanport", "1");
//        fmc.addConfigParam(randomizer,"randomize", "true");
//        tp.init(fmc);
//        randomizer.init(fmc);
//        tp.startUp(fmc);
//
//        // Build our test packet
//        this.testPacket = new Ethernet()
//                .setDestinationMACAddress("00:11:22:33:44:55")
//                .setSourceMACAddress("00:44:33:22:11:00")
//                .setEtherType(EthType.IPv4)
//                .setPayload(
//                        new IPv4()
//                                .setTtl((byte) 128)
//                                .setSourceAddress("10.0.0.1")
//                                .setDestinationAddress("10.0.0.2")
//                                .setPayload(new UDP()
//                                        .setSourcePort((short) 5000)
//                                        .setDestinationPort((short) 5001)
//                                        .setPayload(new Data(new byte[] {0x01}))));
//        this.testPacketSerialized = testPacket.serialize();
//
//        // Build the PacketIn
//        this.packetIn = (OFPacketIn) OFFactories.getFactory(OFVersion.OF_13).buildPacketIn()
//                .setBufferId(OFBufferId.NO_BUFFER)
//                .setMatch(OFFactories.getFactory(OFVersion.OF_13).buildMatch()
//                        .setExact(MatchField.IN_PORT, OFPort.of(1))
//                        .build())
//                .setData(this.testPacketSerialized)
//                .setReason(OFPacketInReason.NO_MATCH)
//                .setTotalLen((short) this.testPacketSerialized.length).build();
//        this.mockSwitch = createMock(IOFSwitch.class);
//        EasyMock.expect(this.mockSwitch.getOFFactory()).andReturn(OFFactories.getFactory(OFVersion.OF_13)).anyTimes();
//    }

//    @Test
//    public void testServiceCalls() throws Exception {
//        randomizer.enable();
//        Assert.assertTrue(randomizer.isEnabled());
//        randomizer.disable();
//        Assert.assertFalse(randomizer.isEnabled());
//    }

//    @Test
//    public void testAddServers() throws Exception {
//        int size = randomizer.getServers().size();
//        randomizer.addServer(new RandomizedHost(IPv4Address.of(10,0,0,50)));
//        Assert.assertEquals(size + 1, randomizer.getServers().size());
//    }

//    @Test
//    public void testAddConnection() throws Exception {
//        RandomizedHost randomizedHost = randomizer.getServers().get(0);
//        DatapathId dpid = DatapathId.of(1);
//        OFPort wanport = OFPort.of(1);
//        OFPort localport = OFPort.of(2);
//        randomizer.addConnection(new Connection(randomizedHost, null, null, dpid, null));
//        randomizer.getConnections().get(0).update();
//    }

}
