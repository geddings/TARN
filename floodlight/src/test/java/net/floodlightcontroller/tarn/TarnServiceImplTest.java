package net.floodlightcontroller.tarn;

import io.netty.buffer.Unpooled;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.test.MockThreadPoolService;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.debugcounter.MockDebugCounterService;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.IEntityClassifierService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.devicemanager.internal.DefaultEntityClassifier;
import net.floodlightcontroller.devicemanager.test.MockDeviceManager;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.tarn.internal.TarnServiceImpl;
import net.floodlightcontroller.test.FloodlightTestCase;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.topology.ITopologyService;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.projectfloodlight.openflow.exceptions.OFParseError;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;

import java.util.Optional;

import static org.easymock.EasyMock.*;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 10/3/17.
 */
public class TarnServiceImplTest extends FloodlightTestCase {

    private TarnServiceImpl randomizer;

    private MockDeviceManager deviceManager;
    private MockThreadPoolService threadPool;
    private ITopologyService topology;
    private IRestApiService restApi;

    private OFFactory factory = OFFactories.getFactory(OFVersion.OF_15);

    @Before
    public void setUp() throws Exception {
        super.setUp();

        randomizer = new TarnServiceImpl();

        /* Mock services */
        mockFloodlightProvider = getMockFloodlightProvider();
        threadPool = new MockThreadPoolService();
        deviceManager = new MockDeviceManager();
        topology = createMock(ITopologyService.class);
        restApi = createMock(IRestApiService.class);
        DefaultEntityClassifier entityClassifier = new DefaultEntityClassifier();


        FloodlightModuleContext fmc = new FloodlightModuleContext();
        fmc.addService(IFloodlightProviderService.class, mockFloodlightProvider);
        fmc.addService(IThreadPoolService.class, threadPool);
        fmc.addService(IDeviceService.class, deviceManager);
        fmc.addService(IDebugCounterService.class, new MockDebugCounterService());
        fmc.addService(ITopologyService.class, topology);
        fmc.addService(IRestApiService.class, restApi);
        fmc.addService(IEntityClassifierService.class, entityClassifier);

        threadPool.init(fmc);
        deviceManager.init(fmc);
        entityClassifier.init(fmc);
        randomizer.init(fmc);

        threadPool.startUp(fmc);
        deviceManager.startUp(fmc);
        entityClassifier.startUp(fmc);
        randomizer.startUp(fmc);
    }

    @Test
    public void testGetAttachmentPoints() {
        MacAddress mac = MacAddress.of(1);
        DatapathId dpid = DatapathId.of(1);
        OFPort port = OFPort.of(1);

        reset(topology);
        expect(topology.isAttachmentPointPort(dpid, port))
                .andReturn(true)
                .anyTimes();
        replay(topology);

        deviceManager.learnEntity(mac, VlanVid.ZERO, IPv4Address.NONE, IPv6Address.NONE, dpid, port);

        Optional<SwitchPort> ap = randomizer.getAttachmentPoint(mac, dpid);

        Assert.assertTrue(ap.isPresent());
        Assert.assertEquals(new SwitchPort(dpid, port), ap.get());
    }

    @Test
    @Ignore
    public void testSessionAddedWhenPacketInHasMapping() {
        randomizer.addPrefixMapping(new PrefixMapping("20.0.0.1", "50.0.0.0/24"));

        IOFSwitch sw = createNiceMock(IOFSwitch.class);
        EasyMock.expect(sw.getId()).andReturn(DatapathId.of(1)).anyTimes();
        EasyMock.replay(sw);

        Match match = factory.buildMatch().setExact(MatchField.IN_PORT, OFPort.of(1)).build();
        OFPacketIn pi = factory.buildPacketIn().setMatch(match).setReason(OFPacketInReason.NO_MATCH).build();

        Ethernet eth = new Ethernet().setSourceMACAddress(MacAddress.of(1)).setDestinationMACAddress(MacAddress.of(2));
        IPv4 iPv4 = new IPv4().setSourceAddress("10.0.0.1").setDestinationAddress("20.0.0.1");
        TCP tcp = new TCP().setSourcePort(40000).setDestinationPort(80);

        iPv4.setProtocol(IpProtocol.TCP).setPayload(tcp);
        eth.setEtherType(EthType.IPv4).setPayload(iPv4);

        FloodlightContext cntx = new FloodlightContext();
        IFloodlightProviderService.bcStore.put(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD, eth);

        randomizer.receive(sw, pi, cntx);
        Assert.assertTrue(!randomizer.getSessions().isEmpty());
    }

    @Test
    @Ignore
    public void testFlowRem() throws OFParseError {
        byte[] flowrem = new byte[] {
                0x06, 0x0b, 0x00, 0x58, 0x00, 0x00, 0x00, 0x00,
                (byte) 0xb2, 0x71, 0x4d, 0x7a, 0x27, 0x35, (byte) 0xca, (byte) 0xbd,
                0x00, 0x64, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d,
                0x06, (byte) 0xac, (byte) 0xfc, 0x00, 0x00, 0x05, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x72,
                0x00, 0x01, 0x00, 0x22, (byte) 0x80, 0x00, 0x00, 0x04,
                0x00, 0x00, 0x00, 0x02, (byte) 0x80, 0x00, 0x0a, 0x02,
                0x08, 0x00, (byte) 0x80, 0x00, 0x16, 0x04, 0x50, 0x00,
                (byte) 0xa2, 0x5e, (byte) 0x80, 0x00, 0x18, 0x04, 0x0a, 0x00,
                0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };

        OFMessage ofMessage = OFFactories.getGenericReader().readFrom(Unpooled.wrappedBuffer(flowrem));
        System.out.println(ofMessage);
    }
}