package net.floodlightcontroller.tarn.types;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.tarn.PacketFlow;
import net.floodlightcontroller.tarn.web.TransportPacketFlowSerializer;
import org.projectfloodlight.openflow.types.*;

/**
 * Created by @geddings on 11/15/17.
 */
@JsonSerialize(using = TransportPacketFlowSerializer.class)
public class TransportPacketFlow implements PacketFlow {

    private final IPVersion ipVersion;
    private final IpProtocol ipProtocol;
    private final IPAddress srcIp;
    private final IPAddress dstIp;
    private final TransportPort srcPort;
    private final TransportPort dstPort;
    private final OFPort inPort;
    private final OFPort outPort;

    private TransportPacketFlow(IPVersion ipVersion, IpProtocol ipProtocol, IPAddress srcIp, IPAddress dstIp, TransportPort srcPort, TransportPort dstPort, OFPort inPort, OFPort outPort) {
        this.ipVersion = ipVersion;
        this.ipProtocol = ipProtocol;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.inPort = inPort;
        this.outPort = outPort;
    }

    @Override
    public IPVersion getIpVersion() {
        return ipVersion;
    }

    public IpProtocol getIpProtocol() {
        return ipProtocol;
    }

    @Override
    public IPAddress getSrcIp() {
        return srcIp;
    }

    @Override
    public IPAddress getDstIp() {
        return dstIp;
    }

    public TransportPort getSrcPort() {
        return srcPort;
    }

    public TransportPort getDstPort() {
        return dstPort;
    }

    @Override
    public OFPort getInPort() {
        return inPort;
    }

    @Override
    public OFPort getOutPort() {
        return outPort;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private IPVersion ipVersion;
        private IpProtocol ipProtocol = IpProtocol.NONE;
        private IPAddress srcIp;
        private IPAddress dstIp;
        private TransportPort srcPort = TransportPort.NONE;
        private TransportPort dstPort = TransportPort.NONE;
        private OFPort inPort = OFPort.ANY;
        private OFPort outPort = OFPort.ANY;

        public Builder ipVersion(IPVersion ipVersion) {
            this.ipVersion = ipVersion;
            return this;
        }
        
        public Builder ipProtocol(IpProtocol ipProtocol) {
            this.ipProtocol = ipProtocol;
            return this;
        }
        
        public Builder srcIp(IPAddress srcIp) {
            this.srcIp = srcIp;
            return this;
        }

        public Builder dstIp(IPAddress dstIp) {
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
            return new TransportPacketFlow(ipVersion, ipProtocol, srcIp, dstIp, srcPort, dstPort, inPort, outPort);
        }
    }
}
