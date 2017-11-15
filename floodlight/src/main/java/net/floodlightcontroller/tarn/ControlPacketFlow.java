package net.floodlightcontroller.tarn;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.tarn.web.ControlPacketFlowSerializer;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;

/**
 * Created by @geddings on 11/15/17.
 */
@JsonSerialize(using = ControlPacketFlowSerializer.class)
public class ControlPacketFlow implements PacketFlow {

    private final IPv4Address srcIp;
    private final IPv4Address dstIp;
    private final OFPort inPort;
    private final OFPort outPort;

    private ControlPacketFlow(IPv4Address srcIp, IPv4Address dstIp, OFPort inPort, OFPort outPort) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.inPort = inPort;
        this.outPort = outPort;
    }

    public IPv4Address getSrcIp() {
        return srcIp;
    }

    public IPv4Address getDstIp() {
        return dstIp;
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
        private IPv4Address srcIp = IPv4Address.NONE;
        private IPv4Address dstIp = IPv4Address.NONE;
        private OFPort inPort = OFPort.ANY;
        private OFPort outPort = OFPort.ANY;

        public Builder srcIp(IPv4Address srcIp) {
            this.srcIp = srcIp;
            return this;
        }

        public Builder dstIp(IPv4Address dstIp) {
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
            return new ControlPacketFlow(srcIp, dstIp, inPort, outPort);
        }
    }
}
