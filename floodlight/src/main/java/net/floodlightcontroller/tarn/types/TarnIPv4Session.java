package net.floodlightcontroller.tarn.types;

import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.tarn.PrefixMapping;
import net.floodlightcontroller.tarn.TarnSession;
import net.floodlightcontroller.tarn.utils.IPGenerator;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/26/18.
 */
public class TarnIPv4Session implements TarnSession<IPv4Address> {
    private static final Logger log = LoggerFactory.getLogger(TarnIPv4Session.class);

    private Direction direction;
    private final OFPort inPort;
    private final OFPort outPort;
    private final IpProtocol ipProtocol;
    private final IPv4Address externalSrcIp;
    private final IPv4Address externalDstIp;
    private final IPv4Address internalSrcIp;
    private final IPv4Address internalDstIp;
    private final TransportPort externalSrcPort;
    private final TransportPort internalSrcPort;
    private final TransportPort externalDstPort;
    private final TransportPort internalDstPort;

    public TarnIPv4Session(IPv4 iPv4, PrefixMapping srcMapping, PrefixMapping dstMapping, OFPort inPort, OFPort
            outPort) {
        this.inPort = inPort;
        this.outPort = outPort;

        if (srcMapping != null) {
            // This means that the IPv4 packet is an egress packet and contains internal addresses
            if (srcMapping.isInternalIp(iPv4.getSourceAddress())) {
                direction = Direction.OUTGOING;
                internalSrcIp = iPv4.getSourceAddress();
                externalSrcIp = IPGenerator.getRandomAddressFrom(srcMapping.getCurrentPrefix());
            } else {
                direction = Direction.INCOMING;
                externalSrcIp = iPv4.getSourceAddress();
                internalSrcIp = srcMapping.getInternalIp();
            }
        } else {
            externalSrcIp = iPv4.getSourceAddress();
            internalSrcIp = iPv4.getSourceAddress();
        }

        if (dstMapping != null) {
            if (dstMapping.isInternalIp(iPv4.getDestinationAddress())) {
                // Traffic coming in is internal traffic: internal -> external
                direction = Direction.OUTGOING;
                internalDstIp = iPv4.getDestinationAddress();
                externalDstIp = IPGenerator.getRandomAddressFrom(dstMapping.getCurrentPrefix());
            } else {
                // Traffic coming in is external traffic: external -> internal
                direction = Direction.INCOMING;
                externalDstIp = iPv4.getDestinationAddress();
                internalDstIp = dstMapping.getInternalIp();
            }
        } else {
            externalDstIp = iPv4.getDestinationAddress();
            internalDstIp = iPv4.getDestinationAddress();
        }

        ipProtocol = iPv4.getProtocol();
        if (iPv4.getProtocol() == IpProtocol.TCP) {
            TCP tcp = (TCP) iPv4.getPayload();
            externalSrcPort = tcp.getSourcePort();
            internalSrcPort = tcp.getSourcePort();
            externalDstPort = tcp.getDestinationPort();
            internalDstPort = tcp.getDestinationPort();
        } else if (iPv4.getProtocol() == IpProtocol.UDP) {
            UDP udp = (UDP) iPv4.getPayload();
            externalSrcPort = udp.getSourcePort();
            internalSrcPort = udp.getSourcePort();
            externalDstPort = udp.getDestinationPort();
            internalDstPort = udp.getDestinationPort();
        } else {
            externalSrcPort = internalSrcPort = externalDstPort = internalDstPort = TransportPort.NONE;
        }
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public OFPort getInPort() {
        return inPort;
    }

    /**
     * Returns the port which all external traffic is received on.
     * @return external port
     */
    @Override
    public OFPort getExternalPort() {
        // If the session has direction incoming (external -> internal),
        // then the in port is facing all external traffic.
        return direction == Direction.INCOMING ? inPort : outPort;
    }

    /**
     * Returns the port which all internal traffic is received on.
     * @return internal port
     */
    @Override
    public OFPort getInternalPort() {
        // If the session has direction outgoing (internal -> external),
        // then the in port is facing all internal traffic.
        return direction == Direction.OUTGOING ? inPort : outPort;
    }

    @Override
    public OFPort getOutPort() {
        return outPort;
    }

    @Override
    public IpProtocol getIpProtocol() {
        return ipProtocol;
    }

    @Override
    public IPv4Address getExternalSrcIp() {
        return externalSrcIp;
    }

    @Override
    public IPv4Address getExternalDstIp() {
        return externalDstIp;
    }

    @Override
    public TransportPort getExternalSrcPort() {
        return externalSrcPort;
    }

    @Override
    public TransportPort getExternalDstPort() {
        return externalDstPort;
    }

    @Override
    public IPv4Address getInternalSrcIp() {
        return internalSrcIp;
    }

    @Override
    public IPv4Address getInternalDstIp() {
        return internalDstIp;
    }

    @Override
    public TransportPort getInternalSrcPort() {
        return internalSrcPort;
    }

    @Override
    public TransportPort getInternalDstPort() {
        return internalDstPort;
    }

    @Override
    public boolean hasTransportPorts() {
        return ipProtocol == IpProtocol.TCP || ipProtocol == IpProtocol.UDP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TarnIPv4Session session = (TarnIPv4Session) o;

        if (direction != session.direction) return false;
        if (inPort != null ? !inPort.equals(session.inPort) : session.inPort != null) return false;
        if (outPort != null ? !outPort.equals(session.outPort) : session.outPort != null) return false;
        if (ipProtocol != null ? !ipProtocol.equals(session.ipProtocol) : session.ipProtocol != null) return false;
        if (externalSrcIp != null ? !externalSrcIp.equals(session.externalSrcIp) : session.externalSrcIp != null)
            return false;
        if (externalDstIp != null ? !externalDstIp.equals(session.externalDstIp) : session.externalDstIp != null)
            return false;
        if (internalSrcIp != null ? !internalSrcIp.equals(session.internalSrcIp) : session.internalSrcIp != null)
            return false;
        if (internalDstIp != null ? !internalDstIp.equals(session.internalDstIp) : session.internalDstIp != null)
            return false;
        if (externalSrcPort != null ? !externalSrcPort.equals(session.externalSrcPort) : session.externalSrcPort !=
                null)
            return false;
        if (internalSrcPort != null ? !internalSrcPort.equals(session.internalSrcPort) : session.internalSrcPort !=
                null)
            return false;
        if (externalDstPort != null ? !externalDstPort.equals(session.externalDstPort) : session.externalDstPort !=
                null)
            return false;
        return internalDstPort != null ? internalDstPort.equals(session.internalDstPort) : session.internalDstPort ==
                null;
    }

    @Override
    public int hashCode() {
        int result = direction != null ? direction.hashCode() : 0;
        result = 31 * result + (inPort != null ? inPort.hashCode() : 0);
        result = 31 * result + (outPort != null ? outPort.hashCode() : 0);
        result = 31 * result + (ipProtocol != null ? ipProtocol.hashCode() : 0);
        result = 31 * result + (externalSrcIp != null ? externalSrcIp.hashCode() : 0);
        result = 31 * result + (externalDstIp != null ? externalDstIp.hashCode() : 0);
        result = 31 * result + (internalSrcIp != null ? internalSrcIp.hashCode() : 0);
        result = 31 * result + (internalDstIp != null ? internalDstIp.hashCode() : 0);
        result = 31 * result + (externalSrcPort != null ? externalSrcPort.hashCode() : 0);
        result = 31 * result + (internalSrcPort != null ? internalSrcPort.hashCode() : 0);
        result = 31 * result + (externalDstPort != null ? externalDstPort.hashCode() : 0);
        result = 31 * result + (internalDstPort != null ? internalDstPort.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "TarnIPv4Session{" +
                "direction=" + direction +
                ", inPort=" + inPort +
                ", outPort=" + outPort +
                ", ipProtocol=" + ipProtocol +
                ", externalSrcIp=" + externalSrcIp +
                ", externalDstIp=" + externalDstIp +
                ", internalSrcIp=" + internalSrcIp +
                ", internalDstIp=" + internalDstIp +
                ", externalSrcPort=" + externalSrcPort +
                ", internalSrcPort=" + internalSrcPort +
                ", externalDstPort=" + externalDstPort +
                ", internalDstPort=" + internalDstPort +
                '}';
    }
}
