package net.floodlightcontroller.tarn;

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
}
