package net.floodlightcontroller.tarn.types;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.tarn.PacketFlow;
import net.floodlightcontroller.tarn.web.ControlPacketFlowSerializer;
import org.projectfloodlight.openflow.types.IPAddress;
import org.projectfloodlight.openflow.types.IPVersion;
import org.projectfloodlight.openflow.types.OFPort;

/**
 * Created by @geddings on 11/15/17.
 */
@JsonSerialize(using = ControlPacketFlowSerializer.class)
public class ControlPacketFlow implements PacketFlow {

    private final IPVersion ipVersion;
    private final IPAddress srcIp;
    private final IPAddress dstIp;
    private final OFPort inPort;
    private final OFPort outPort;

    private ControlPacketFlow(IPVersion ipVersion, IPAddress srcIp, IPAddress dstIp, OFPort inPort, OFPort outPort) {
        this.ipVersion = ipVersion;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.inPort = inPort;
        this.outPort = outPort;
    }

    @Override
    public IPVersion getIpVersion() {
        return ipVersion;
    }

    @Override
    public IPAddress getSrcIp() {
        return srcIp;
    }

    @Override
    public IPAddress getDstIp() {
        return dstIp;
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
        private IPAddress srcIp;
        private IPAddress dstIp;
        private OFPort inPort = OFPort.ANY;
        private OFPort outPort = OFPort.ANY;
        
        public Builder ipVersion(IPVersion ipVersion) {
            this.ipVersion = ipVersion;
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

        public Builder inPort(OFPort inPort) {
            this.inPort = inPort;
            return this;
        }

        public Builder outPort(OFPort outPort) {
            this.outPort = outPort;
            return this;
        }

        public ControlPacketFlow build() {
            return new ControlPacketFlow(ipVersion, srcIp, dstIp, inPort, outPort);
        }
    }
}
