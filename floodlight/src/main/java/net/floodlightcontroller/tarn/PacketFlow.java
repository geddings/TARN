package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;

/**
 * Created by @geddings on 11/15/17.
 */
public interface PacketFlow {
    
    IPv4Address getSrcIp();
    IPv4Address getDstIp();
    OFPort getInPort();
    OFPort getOutPort();
    
}
