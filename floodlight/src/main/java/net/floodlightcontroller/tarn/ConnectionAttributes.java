package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/2/17.
 */
public class ConnectionAttributes {

    private final IPv4Address srcIp;
    private final IPv4Address dstIp;
    private final OFPort srcPort;
    private final OFPort dstPort;

    private ConnectionAttributes(IPv4Address srcIp, IPv4Address dstIp, OFPort srcPort, OFPort dstPort) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
    }

    public IPv4Address getSrcIp() {
        return srcIp;
    }

    public IPv4Address getDstIp() {
        return dstIp;
    }

    public OFPort getSrcPort() {
        return srcPort;
    }

    public OFPort getDstPort() {
        return dstPort;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        IPv4Address srcIp = IPv4Address.NONE;
        IPv4Address dstIp = IPv4Address.NONE;
        OFPort srcPort = OFPort.ANY;
        OFPort dstPort = OFPort.ANY;

        public Builder srcIp(IPv4Address srcIp) {
            this.srcIp = srcIp;
            return this;
        }

        public Builder dstIp(IPv4Address dstIp) {
            this.dstIp = dstIp;
            return this;
        }

        public Builder srcPort(OFPort srcPort) {
            this.srcPort = srcPort;
            return this;
        }

        public Builder dstPort(OFPort dstPort) {
            this.dstPort = dstPort;
            return this;
        }

        public ConnectionAttributes build() {
            return new ConnectionAttributes(srcIp, dstIp, srcPort, dstPort);
        }
    }
}
