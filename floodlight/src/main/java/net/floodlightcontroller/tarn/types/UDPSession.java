package net.floodlightcontroller.tarn.types;

import net.floodlightcontroller.tarn.Session;

/**
 * Created by @geddings on 11/15/17.
 */
public class UDPSession implements Session {
    
    private final TransportPacketFlow inbound;
    private final TransportPacketFlow outbound;

    public UDPSession(TransportPacketFlow inbound, TransportPacketFlow outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    @Override
    public TransportPacketFlow getInbound() {
        return inbound;
    }

    @Override
    public TransportPacketFlow getOutbound() {
        return outbound;
    }
}
