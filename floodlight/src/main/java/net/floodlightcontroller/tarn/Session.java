package net.floodlightcontroller.tarn;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.tarn.web.SessionSerializer;

/**
 * Created by @geddings on 11/15/17.
 */
@JsonSerialize(using = SessionSerializer.class)
public interface Session {
    PacketFlow getInbound();

    PacketFlow getOutbound();
}
