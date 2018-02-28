package net.floodlightcontroller.tarn.types;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.packet.IPv6;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.tarn.PrefixMapping;
import net.floodlightcontroller.tarn.TarnSession;
import net.floodlightcontroller.tarn.utils.IPUtils;
import net.floodlightcontroller.tarn.web.TarnSessionSerializer;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

import java.util.UUID;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/26/18.
 */
@JsonSerialize(using = TarnSessionSerializer.class)
public class TarnIPv6Session implements TarnSession<IPv6Address> {

    private UUID id;
    private Status status;
    private Direction direction;
    private final OFPort inPort;
    private final OFPort outPort;
    private final IpProtocol ipProtocol;
    private IPv6Address externalSrcIp;
    private IPv6Address externalDstIp;
    private IPv6Address internalSrcIp;
    private IPv6Address internalDstIp;
    private final TransportPort externalSrcPort;
    private final TransportPort internalSrcPort;
    private final TransportPort externalDstPort;
    private final TransportPort internalDstPort;

    public TarnIPv6Session(IPv6 iPv6, PrefixMapping srcMapping, PrefixMapping dstMapping, OFPort inPort, OFPort
            outPort) {
        id = UUID.randomUUID();

        status = Status.ACTIVE;

        this.inPort = inPort;
        this.outPort = outPort;

        if (srcMapping != null) {
            // This means that the IPv4 packet is an egress packet and contains internal addresses
            if (srcMapping.isInternalIp(iPv6.getSourceAddress())) {
                direction = Direction.OUTGOING;
                internalSrcIp = iPv6.getSourceAddress();
                externalDstIp = (IPv6Address) IPUtils.getRandomAddressFrom(srcMapping.getCurrentPrefix());
            } else {
                direction = Direction.INCOMING;
                externalSrcIp = iPv6.getSourceAddress();
                internalDstIp = (IPv6Address) srcMapping.getInternalIp();
            }
        }

        if (dstMapping != null) {
            if (dstMapping.isInternalIp(iPv6.getDestinationAddress())) {
                // Traffic coming in is internal traffic: internal -> external
                direction = Direction.OUTGOING;
                internalDstIp = iPv6.getDestinationAddress();
                externalSrcIp = (IPv6Address) IPUtils.getRandomAddressFrom(dstMapping.getCurrentPrefix());
            } else {
                // Traffic coming in is external traffic: external -> internal
                direction = Direction.INCOMING;
                externalDstIp = iPv6.getDestinationAddress();
                internalSrcIp = (IPv6Address) dstMapping.getInternalIp();
            }
        }

        if (internalSrcIp == null && externalDstIp == null) {
            if (direction == Direction.OUTGOING) {
                internalSrcIp = externalDstIp = iPv6.getSourceAddress();
            } else {
                internalSrcIp = externalDstIp = iPv6.getDestinationAddress();
            }
        } else if (internalDstIp == null && externalSrcIp == null) {
            if (direction == Direction.OUTGOING) {
                internalDstIp = externalSrcIp = iPv6.getDestinationAddress();
            } else {
                internalDstIp = externalSrcIp = iPv6.getSourceAddress();
            }
        }

        ipProtocol = iPv6.getNextHeader();
        if (ipProtocol == IpProtocol.TCP) {
            TCP tcp = (TCP) iPv6.getPayload();
            externalSrcPort = tcp.getSourcePort();
            internalSrcPort = tcp.getSourcePort();
            externalDstPort = tcp.getDestinationPort();
            internalDstPort = tcp.getDestinationPort();
        } else if (ipProtocol == IpProtocol.UDP) {
            UDP udp = (UDP) iPv6.getPayload();
            externalSrcPort = udp.getSourcePort();
            internalSrcPort = udp.getSourcePort();
            externalDstPort = udp.getDestinationPort();
            internalDstPort = udp.getDestinationPort();
        } else {
            externalSrcPort = internalSrcPort = externalDstPort = internalDstPort = TransportPort.NONE;
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
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
     *
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
     *
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
    public IPv6Address getExternalSrcIp() {
        return externalSrcIp;
    }

    @Override
    public IPv6Address getExternalDstIp() {
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
    public IPv6Address getInternalSrcIp() {
        return internalSrcIp;
    }

    @Override
    public IPv6Address getInternalDstIp() {
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

        TarnIPv6Session session = (TarnIPv6Session) o;

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
        return "TarnIPv6Session{" +
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
