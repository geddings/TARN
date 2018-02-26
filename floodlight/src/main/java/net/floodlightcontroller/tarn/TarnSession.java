package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPAddress;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/26/18.
 */
public interface TarnSession<T extends IPAddress> {

    /**
     * INCOMING: Traffic coming in from the Internet (i.e. external -> internal)
     * OUTGOING: Traffic is going out to the Internet (i.e. internal -> external)
     */
    enum Direction {
        INCOMING, OUTGOING
    }

    Direction getDirection();

    OFPort getInPort();

    OFPort getOutPort();

    OFPort getExternalPort();

    OFPort getInternalPort();

    IpProtocol getIpProtocol();

    T getExternalSrcIp();

    T getExternalDstIp();

    TransportPort getExternalSrcPort();

    TransportPort getExternalDstPort();

    T getInternalSrcIp();

    T getInternalDstIp();

    TransportPort getInternalSrcPort();

    TransportPort getInternalDstPort();

    boolean hasTransportPorts();
}
