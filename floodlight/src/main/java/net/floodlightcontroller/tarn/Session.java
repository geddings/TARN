package net.floodlightcontroller.tarn;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.tarn.web.SessionSerializer;

/**
 * Represents a TARN session. A TARN session is defined as a complete two-way communication between two devices, with
 * at least one device being a TARN device (i.e. has a configured prefix mapping).
 *
 * A TARN session will look different depending on where it is being viewed from. Because of this, the two views must
 * be defined. The terms inbound and outbound are used here to define the two vantage points, as they relate to the
 * local network.
 *
 * From the 'inbound' perspective, or coming 'in' from outside the local network, the connection
 * attributes will always contain a TARN device's external address. Likewise, from the 'outbound' perspective, or going
 * 'out' of the the local network, the connection attributes will always contain a TARN device's internal address. Each
 * perspective is from the vantage point of Floodlight, before any rewrites occur, in each direction.
 *
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/15/17.
 */
@JsonSerialize(using = SessionSerializer.class)
public interface Session {
    PacketFlow getInbound();

    PacketFlow getOutbound();
}
