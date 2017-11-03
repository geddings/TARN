package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.test.MockThreadPoolService;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.debugcounter.MockDebugCounterService;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.IEntityClassifierService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.devicemanager.internal.DefaultEntityClassifier;
import net.floodlightcontroller.devicemanager.test.MockDeviceManager;
import net.floodlightcontroller.test.FloodlightTestCase;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.topology.ITopologyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.types.*;

import java.util.Optional;

import static org.easymock.EasyMock.*;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 10/3/17.
 */
public class RandomizerServiceTest extends FloodlightTestCase {

    private RandomizerService randomizer;

    private MockDeviceManager deviceManager;
    protected MockThreadPoolService threadPool;
    protected ITopologyService topology;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        randomizer = new RandomizerService();

        /* Mock services */
        mockFloodlightProvider = getMockFloodlightProvider();
        threadPool = new MockThreadPoolService();
        deviceManager = new MockDeviceManager();
        topology = createMock(ITopologyService.class);
        DefaultEntityClassifier entityClassifier = new DefaultEntityClassifier();


        FloodlightModuleContext fmc = new FloodlightModuleContext();
        fmc.addService(IFloodlightProviderService.class, mockFloodlightProvider);
        fmc.addService(IThreadPoolService.class, threadPool);
        fmc.addService(IDeviceService.class, deviceManager);
        fmc.addService(IDebugCounterService.class, new MockDebugCounterService());
        fmc.addService(ITopologyService.class, topology);
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
    public void testBuildSession() {


//        randomizer.buildSession()
    }
}