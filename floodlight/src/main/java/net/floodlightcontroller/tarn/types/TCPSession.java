package net.floodlightcontroller.tarn.types;

import net.floodlightcontroller.tarn.Session;

/**
 * Created by @geddings on 11/15/17.
 */
public class TCPSession implements Session {
    
    private final TransportPacketFlow inbound;
    private final TransportPacketFlow outbound;

    public TCPSession(TransportPacketFlow inbound, TransportPacketFlow outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    public TransportPacketFlow getInbound() {
        return inbound;
    }

    public TransportPacketFlow getOutbound() {
        return outbound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TCPSession that = (TCPSession) o;

        if (inbound != null ? !inbound.equals(that.inbound) : that.inbound != null) return false;
        return outbound != null ? outbound.equals(that.outbound) : that.outbound == null;

    }

    @Override
    public int hashCode() {
        int result = inbound != null ? inbound.hashCode() : 0;
        result = 31 * result + (outbound != null ? outbound.hashCode() : 0);
        return result;
    }
}
