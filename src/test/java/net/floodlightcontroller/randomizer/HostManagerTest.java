package net.floodlightcontroller.randomizer;

import net.floodlightcontroller.test.FloodlightTestCase;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.IPv4Address;

/**
 * Created by geddingsbarrineau on 2/2/17.
 */
public class HostManagerTest extends FloodlightTestCase{

    RandomizedHost randomizedHost;
    HostManager sm;
    OFFactory factory;
    
    @Before
    public void SetUp() throws Exception {
        super.setUp();
        sm = new HostManager();
        randomizedHost = new RandomizedHost(IPv4Address.of(10, 0, 0, 4));
        sm.addHost(randomizedHost);
        factory = OFFactories.getFactory(OFVersion.OF_13);
    }
    
    @Test
    public void testGetServerThatContainsIP() {
//        RandomizedHost actual = sm.getServerThatContainsIP(IPv4Address.of("184.164.243.69"));
//        Assert.assertEquals(randomizedHost, actual);
    }
    
}
