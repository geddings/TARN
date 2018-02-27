package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPAddress;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

import java.util.UUID;

/**
 * Represents a TARN session. A TARN session is defined as a complete two-way communication between two devices, with
 * at least one device being a TARN device (i.e. has a configured prefix mapping).
 *
 * A TARN session will look different depending on where it is being viewed from. Because of this, the two views must
 * be defined. The terms inbound and outbound are used here to define the two vantage points, as they relate to the
 * local network.
 *
 * From the 'incoming' perspective, or coming 'in' from outside the local network, the connection
 * attributes will always contain a TARN device's external address. Likewise, from the 'outgoing' perspective, or going
 * 'out' of the the local network, the connection attributes will always contain a TARN device's internal address. Each
 * perspective is from the vantage point of Floodlight, before any rewrites occur, in each direction.
 *
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

    enum Status {
        ACTIVE, INACTIVE
    }

    UUID getId();

    Status getStatus();

    void setStatus(Status status);

    Direction getDirection();

    OFPort getInPort();

    OFPort getOutPort();

    OFPort getExternalPort();

    OFPort getInternalPort();

    IpProtocol getIpProtocol();

    T getInternalSrcIp();

    T getInternalDstIp();

    T getExternalSrcIp();

    T getExternalDstIp();

    TransportPort getInternalSrcPort();

    TransportPort getInternalDstPort();

    TransportPort getExternalSrcPort();

    TransportPort getExternalDstPort();

    boolean hasTransportPorts();
}
