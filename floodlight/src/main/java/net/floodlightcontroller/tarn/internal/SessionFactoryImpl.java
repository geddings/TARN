package net.floodlightcontroller.tarn.internal;

import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.IPv6;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.tarn.PrefixMapping;
import net.floodlightcontroller.tarn.PrefixMappingHandler;
import net.floodlightcontroller.tarn.Session;
import net.floodlightcontroller.tarn.SessionFactory;
import net.floodlightcontroller.tarn.types.*;
import net.floodlightcontroller.tarn.utils.IPGenerator;
import org.projectfloodlight.openflow.types.*;

import java.util.Optional;

/**
 * Created by @geddings on 11/15/17.
 */
public class SessionFactoryImpl implements SessionFactory {

    private final PrefixMappingHandler mappingHandler;

    public SessionFactoryImpl(PrefixMappingHandler mappingHandler) {
        this.mappingHandler = mappingHandler;
    }

    @Override
    public Session getSession(OFPort inPort, OFPort outPort, IPv4 ipv4) {

        if (ipv4.getProtocol() == IpProtocol.TCP) {
            TCP tcp = (TCP) ipv4.getPayload();
            return buildTCPSession(ipv4.getSourceAddress(), ipv4.getDestinationAddress(), IPVersion.IPv4, inPort, outPort, tcp);
        } else if (ipv4.getProtocol() == IpProtocol.UDP) {
            UDP udp = (UDP) ipv4.getPayload();
            return buildUDPSession(inPort, outPort, ipv4, udp);
        } else if (ipv4.getProtocol() == IpProtocol.ICMP) {
            return buildICMPSession(inPort, outPort, ipv4);
        }

        return null;
    }

    public Session getSession(OFPort inPort, OFPort outPort, IPv6 ipv6) {
        
        if (ipv6.getNextHeader() == IpProtocol.TCP) {
            TCP tcp = (TCP) ipv6.getPayload();
            return buildTCPSession(ipv6.getSourceAddress(), ipv6.getDestinationAddress(), IPVersion.IPv6, inPort, outPort, tcp);
        } else if (ipv6.getNextHeader() == IpProtocol.UDP) {
            UDP udp = (UDP) ipv6.getPayload();
            //return buildUDPSession(inPort, outPort, ipv6, udp);
        } else if (ipv6.getNextHeader() == IpProtocol.ICMP) {
            //return buildICMPSession(inPort, outPort, ipv6);
        }

        return null;
    }

    /**
     * Builds a new TARN session object based on the various payloads of a packet in message.
     *
     * @param sw     the switch that the message was received on.
     * @param inPort the port that the message was received on
     * @param eth    the ethernet payload of the packet in message
     * @param ipv4   the ipv4 payload of the packet in message
     * @param tcp    the tcp payload of the packet in message
     * @return a new session object
     */
    private TCPSession buildTCPSession(IPAddress srcIp, IPAddress dstIp, IPVersion ipVersion, OFPort inPort, OFPort outPort, TCP tcp) {
        TransportPacketFlow.Builder connection1 = TransportPacketFlow.builder();
        TransportPacketFlow.Builder connection2 = TransportPacketFlow.builder();

        connection1.ipVersion(ipVersion).ipProtocol(IpProtocol.TCP);
        connection2.ipVersion(ipVersion).ipProtocol(IpProtocol.TCP);

        /* Start filling in information about the two connections from the packet in message.
        *  Connection1 will always be the connection that the packet in message belongs to and
        *  Connection2 will always be the opposite connection. It can't yet be known which connection
        *  is inbound and which is outbound. */
        connection1.inPort(inPort)
                .srcIp(srcIp)
                .dstIp(dstIp)
                .srcPort(tcp.getSourcePort())
                .dstPort(tcp.getDestinationPort());

        connection2.outPort(inPort)
                .srcPort(tcp.getDestinationPort())
                .dstPort(tcp.getSourcePort());

        connection1.outPort(outPort);
        connection2.inPort(outPort);
        
        /* Using the source address of Connection1, determine the destination address of Connection2. */
        connection2.dstIp(getReturnAddress(srcIp));

        /* Using the destination address of Connection1, determine the source address of Connection2. */
        connection2.srcIp(getReturnAddress(dstIp));

        /* Determine which connection is inbound and which is outbound.
         * An outbound connection is one that will match on INTERNAL IP addresses.
         * An inbound connection is one that will match on EXTERNAL IP addresses. */
        if (mappingHandler.isInternalIp(srcIp) || mappingHandler.isInternalIp(dstIp)) {
            return new TCPSession(connection2.build(), connection1.build());
        } else {
            return new TCPSession(connection1.build(), connection2.build());
        }
    }

    private UDPSession buildUDPSession(OFPort inPort, OFPort outPort, IPv4 ipv4, UDP udp) {
        TransportPacketFlow.Builder connection1 = TransportPacketFlow.builder();
        TransportPacketFlow.Builder connection2 = TransportPacketFlow.builder();

        connection1.ipVersion(IPVersion.IPv4).ipProtocol(IpProtocol.UDP);
        connection2.ipVersion(IPVersion.IPv4).ipProtocol(IpProtocol.UDP);        
        
        /* Start filling in information about the two connections from the packet in message.
        *  Connection1 will always be the connection that the packet in message belongs to and
        *  Connection2 will always be the opposite connection. It can't yet be known which connection
        *  is inbound and which is outbound. */
        connection1.inPort(inPort)
                .srcIp(ipv4.getSourceAddress())
                .dstIp(ipv4.getDestinationAddress())
                .srcPort(udp.getSourcePort())
                .dstPort(udp.getDestinationPort());

        connection2.outPort(inPort)
                .srcPort(udp.getDestinationPort())
                .dstPort(udp.getSourcePort());

        connection1.outPort(outPort);
        connection2.inPort(outPort);

        /* Using the source address of Connection1, determine the destination address of Connection2. */
        connection2.dstIp(getReturnAddress(ipv4.getSourceAddress()));

        /* Using the destination address of Connection1, determine the source address of Connection2. */
        connection2.srcIp(getReturnAddress(ipv4.getDestinationAddress()));

        /* Determine which connection is inbound and which is outbound.
         * An outbound connection is one that will match on INTERNAL IP addresses.
         * An inbound connection is one that will match on EXTERNAL IP addresses. */
        if (mappingHandler.isInternalIp(ipv4.getSourceAddress()) || mappingHandler.isInternalIp(ipv4.getDestinationAddress())) {
            return new UDPSession(connection2.build(), connection1.build());
        } else {
            return new UDPSession(connection1.build(), connection2.build());
        }
    }

    private ICMPSession buildICMPSession(OFPort inPort, OFPort outPort, IPv4 ipv4) {
        ControlPacketFlow.Builder connection1 = ControlPacketFlow.builder();
        ControlPacketFlow.Builder connection2 = ControlPacketFlow.builder();

        connection1.ipVersion(IPVersion.IPv4);
        connection2.ipVersion(IPVersion.IPv4);
        
        /* Start filling in information about the two connections from the packet in message.
        *  Connection1 will always be the connection that the packet in message belongs to and
        *  Connection2 will always be the opposite connection. It can't yet be known which connection
        *  is inbound and which is outbound. */
        connection1.inPort(inPort)
                .srcIp(ipv4.getSourceAddress())
                .dstIp(ipv4.getDestinationAddress());

        connection2.outPort(inPort);

        connection1.outPort(outPort);
        connection2.inPort(outPort);

        /* Using the source address of Connection1, determine the destination address of Connection2. */
        connection2.dstIp(getReturnAddress(ipv4.getSourceAddress()));

        /* Using the destination address of Connection1, determine the source address of Connection2. */
        connection2.srcIp(getReturnAddress(ipv4.getDestinationAddress()));

        /* Determine which connection is inbound and which is outbound.
         * An outbound connection is one that will match on INTERNAL IP addresses.
         * An inbound connection is one that will match on EXTERNAL IP addresses. */
        if (mappingHandler.isInternalIp(ipv4.getSourceAddress()) || mappingHandler.isInternalIp(ipv4.getDestinationAddress())) {
            return new ICMPSession(connection2.build(), connection1.build());
        } else {
            return new ICMPSession(connection1.build(), connection2.build());
        }
    }
    
    private IPAddress getReturnAddress(IPAddress ipAddress) {
        Optional<PrefixMapping> mapping = mappingHandler.getAssociatedMapping(ipAddress);
        if (mapping.isPresent()) {
            IPAddressWithMask currentPrefix = mapping.get().getCurrentPrefix();
            if (mapping.get().getInternalIp().equals(ipAddress)) {
                return IPGenerator.getRandomAddressFrom(currentPrefix);
            } else if (currentPrefix.contains(ipAddress)) {
                return mapping.get().getInternalIp();
            }
        }
        return ipAddress;
    }
}
