package net.floodlightcontroller.tarn;

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
}
