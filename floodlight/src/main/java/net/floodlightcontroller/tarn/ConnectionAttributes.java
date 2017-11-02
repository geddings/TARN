package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/2/17.
 */
public class ConnectionAttributes {

    private final IPv4Address srcIp;
    private final IPv4Address dstIp;
    private final TransportPort srcPort;
    private final TransportPort dstPort;
    private final OFPort inPort;
    private final OFPort outPort;

    private ConnectionAttributes(IPv4Address srcIp, IPv4Address dstIp, TransportPort srcPort, TransportPort dstPort, OFPort inPort, OFPort outPort) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.inPort = inPort;
        this.outPort = outPort;
    }

    public IPv4Address getSrcIp() {
        return srcIp;
    }

    public IPv4Address getDstIp() {
        return dstIp;
    }

    public TransportPort getSrcPort() {
        return srcPort;
    }

    public TransportPort getDstPort() {
        return dstPort;
    }

    public OFPort getInPort() {
        return inPort;
    }

    public OFPort getOutPort() {
        return outPort;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        IPv4Address srcIp = IPv4Address.NONE;
        IPv4Address dstIp = IPv4Address.NONE;
        TransportPort srcPort = TransportPort.NONE;
        TransportPort dstPort = TransportPort.NONE;
        OFPort inPort = OFPort.ANY;
        OFPort outPort = OFPort.ANY;

        public Builder srcIp(IPv4Address srcIp) {
            this.srcIp = srcIp;
            return this;
        }

        public Builder dstIp(IPv4Address dstIp) {
            this.dstIp = dstIp;
            return this;
        }

        public Builder srcPort(TransportPort srcPort) {
            this.srcPort = srcPort;
            return this;
        }

        public Builder dstPort(TransportPort dstPort) {
            this.dstPort = dstPort;
            return this;
        }

        public Builder inPort(OFPort inPort) {
            this.inPort = inPort;
            return this;
        }

        public Builder outPort(OFPort outPort) {
            this.outPort = outPort;
            return this;
        }

        public ConnectionAttributes build() {
            return new ConnectionAttributes(srcIp, dstIp, srcPort, dstPort, inPort, outPort);
        }
    }
}