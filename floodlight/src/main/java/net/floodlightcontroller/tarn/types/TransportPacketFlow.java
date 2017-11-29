package net.floodlightcontroller.tarn.types;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.tarn.PacketFlow;
import net.floodlightcontroller.tarn.web.TransportPacketFlowSerializer;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

/**
 * Created by @geddings on 11/15/17.
 */
@JsonSerialize(using = TransportPacketFlowSerializer.class)
public class TransportPacketFlow implements PacketFlow {

    private final IpProtocol ipProtocol;
    private final IPv4Address srcIp;
    private final IPv4Address dstIp;
    private final TransportPort srcPort;
    private final TransportPort dstPort;
    private final OFPort inPort;
    private final OFPort outPort;

    private TransportPacketFlow(IpProtocol ipProtocol, IPv4Address srcIp, IPv4Address dstIp, TransportPort srcPort, TransportPort dstPort, OFPort inPort, OFPort outPort) {
        this.ipProtocol = ipProtocol;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.inPort = inPort;
        this.outPort = outPort;
    }

    public IpProtocol getIpProtocol() {
        return ipProtocol;
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
        private IpProtocol ipProtocol = IpProtocol.NONE;
        private IPv4Address srcIp = IPv4Address.NONE;
        private IPv4Address dstIp = IPv4Address.NONE;
        private TransportPort srcPort = TransportPort.NONE;
        private TransportPort dstPort = TransportPort.NONE;
        private OFPort inPort = OFPort.ANY;
        private OFPort outPort = OFPort.ANY;

        public Builder ipProtocol(IpProtocol ipProtocol) {
            this.ipProtocol = ipProtocol;
            return this;
        }
        
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

        public TransportPacketFlow build() {
            return new TransportPacketFlow(ipProtocol, srcIp, dstIp, srcPort, dstPort, inPort, outPort);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransportPacketFlow that = (TransportPacketFlow) o;

        if (ipProtocol != null ? !ipProtocol.equals(that.ipProtocol) : that.ipProtocol != null) return false;
        if (srcIp != null ? !srcIp.equals(that.srcIp) : that.srcIp != null) return false;
        if (dstIp != null ? !dstIp.equals(that.dstIp) : that.dstIp != null) return false;
        if (srcPort != null ? !srcPort.equals(that.srcPort) : that.srcPort != null) return false;
        if (dstPort != null ? !dstPort.equals(that.dstPort) : that.dstPort != null) return false;
        if (inPort != null ? !inPort.equals(that.inPort) : that.inPort != null) return false;
        return outPort != null ? outPort.equals(that.outPort) : that.outPort == null;

    }

    @Override
    public int hashCode() {
        int result = ipProtocol != null ? ipProtocol.hashCode() : 0;
        result = 31 * result + (srcIp != null ? srcIp.hashCode() : 0);
        result = 31 * result + (dstIp != null ? dstIp.hashCode() : 0);
        result = 31 * result + (srcPort != null ? srcPort.hashCode() : 0);
        result = 31 * result + (dstPort != null ? dstPort.hashCode() : 0);
        result = 31 * result + (inPort != null ? inPort.hashCode() : 0);
        result = 31 * result + (outPort != null ? outPort.hashCode() : 0);
        return result;
    }
}
