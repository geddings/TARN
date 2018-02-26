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

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/26/18.
 */
public class TarnIPv4Session implements TarnSession<IPv4Address> {

    private Direction direction;
    private final OFPort inPort;
    private final OFPort outPort;
    private final IpProtocol ipProtocol;
    private final IPv4Address ingressSrcIp;
    private final IPv4Address ingressDstIp;
    private final IPv4Address egressSrcIp;
    private final IPv4Address egressDstIp;
    private final TransportPort ingressSrcPort;
    private final TransportPort egressSrcPort;
    private final TransportPort ingressDstPort;
    private final TransportPort egressDstPort;

    public TarnIPv4Session(IPv4 iPv4, PrefixMapping srcMapping, PrefixMapping dstMapping, OFPort inPort, OFPort
            outPort) {
        this.inPort = inPort;
        this.outPort = outPort;

        if (srcMapping != null) {
            // This means that the IPv4 packet is an egress packet and contains internal addresses
            if (srcMapping.isInternalIp(iPv4.getSourceAddress())) {
                direction = Direction.OUTGOING;
                egressSrcIp = iPv4.getSourceAddress();
                ingressSrcIp = IPGenerator.getRandomAddressFrom(srcMapping.getCurrentPrefix());
            } else {
                direction = Direction.INCOMING;
                ingressSrcIp = iPv4.getSourceAddress();
                egressSrcIp = srcMapping.getInternalIp();
            }
        } else {
            ingressSrcIp = iPv4.getSourceAddress();
            egressSrcIp = iPv4.getSourceAddress();
        }

        if (dstMapping != null) {
            // This means that the IPv4 packet is an egress packet and contains internal addresses
            if (dstMapping.isInternalIp(iPv4.getSourceAddress())) {
                direction = Direction.OUTGOING;
                egressDstIp = iPv4.getSourceAddress();
                ingressDstIp = IPGenerator.getRandomAddressFrom(dstMapping.getCurrentPrefix());
            } else {
                direction = Direction.INCOMING;
                ingressDstIp = iPv4.getSourceAddress();
                egressDstIp = dstMapping.getInternalIp();
            }
        } else {
            ingressDstIp = iPv4.getDestinationAddress();
            egressDstIp = iPv4.getDestinationAddress();
        }

        ipProtocol = iPv4.getProtocol();
        if (iPv4.getProtocol() == IpProtocol.TCP) {
            TCP tcp = (TCP) iPv4.getPayload();
            ingressSrcPort = tcp.getSourcePort();
            egressSrcPort = tcp.getSourcePort();
            ingressDstPort = tcp.getDestinationPort();
            egressDstPort = tcp.getDestinationPort();
        } else if (iPv4.getProtocol() == IpProtocol.UDP) {
            UDP udp = (UDP) iPv4.getPayload();
            ingressSrcPort = udp.getSourcePort();
            egressSrcPort = udp.getSourcePort();
            ingressDstPort = udp.getDestinationPort();
            egressDstPort = udp.getDestinationPort();
        } else {
            ingressSrcPort = egressSrcPort = ingressDstPort = egressDstPort = TransportPort.NONE;
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

    @Override
    public OFPort getIngressPort() {
        return direction == Direction.INCOMING ? outPort : inPort;
    }

    @Override
    public OFPort getEgressPort() {
        return direction == Direction.INCOMING ? inPort : outPort;
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
    public IPv4Address getIngressSrcIp() {
        return ingressSrcIp;
    }

    @Override
    public IPv4Address getIngressDstIp() {
        return ingressDstIp;
    }

    @Override
    public TransportPort getIngressSrcPort() {
        return ingressSrcPort;
    }

    @Override
    public TransportPort getIngressDstPort() {
        return ingressDstPort;
    }

    @Override
    public IPv4Address getEgressSrcIp() {
        return egressSrcIp;
    }

    @Override
    public IPv4Address getEgressDstIp() {
        return egressDstIp;
    }

    @Override
    public TransportPort getEgressSrcPort() {
        return egressSrcPort;
    }

    @Override
    public TransportPort getEgressDstPort() {
        return egressDstPort;
    }

    @Override
    public boolean hasTransportPorts() {
        return ipProtocol == IpProtocol.TCP || ipProtocol == IpProtocol.UDP;
    }
}
