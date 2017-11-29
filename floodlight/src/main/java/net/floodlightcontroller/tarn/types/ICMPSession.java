package net.floodlightcontroller.tarn.types;

import net.floodlightcontroller.tarn.Session;

/**
 * Created by @geddings on 11/15/17.
 */
public class ICMPSession implements Session {
    
    private final ControlPacketFlow inbound;
    private final ControlPacketFlow outbound;

    public ICMPSession(ControlPacketFlow inbound, ControlPacketFlow outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    public ControlPacketFlow getInbound() {
        return inbound;
    }

    public ControlPacketFlow getOutbound() {
        return outbound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ICMPSession that = (ICMPSession) o;

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
