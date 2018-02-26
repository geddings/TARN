package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPAddress;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/26/18.
 */
public interface TarnSession<T extends IPAddress> {

    enum Direction {
        INCOMING, OUTGOING
    }

    Direction getDirection();

    OFPort getInPort();

    OFPort getOutPort();

    OFPort getIngressPort();

    OFPort getEgressPort();

    IpProtocol getIpProtocol();

    T getIngressSrcIp();

    T getIngressDstIp();

    TransportPort getIngressSrcPort();

    TransportPort getIngressDstPort();

    T getEgressSrcIp();

    T getEgressDstIp();

    TransportPort getEgressSrcPort();

    TransportPort getEgressDstPort();

    boolean hasTransportPorts();
}
