package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPAddress;
import org.projectfloodlight.openflow.types.IPVersion;
import org.projectfloodlight.openflow.types.OFPort;

/**
 * Created by @geddings on 11/15/17.
 */
public interface PacketFlow {
    
    IPVersion getIpVersion();
    IPAddress getSrcIp();
    IPAddress getDstIp();
    OFPort getInPort();
    OFPort getOutPort();
    
}
