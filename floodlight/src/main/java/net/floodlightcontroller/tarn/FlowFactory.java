package net.floodlightcontroller.tarn;

import net.floodlightcontroller.tarn.types.TarnIPv4Session;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.types.U64;

import java.util.List;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/2/17.
 */
public interface FlowFactory {

    U64 OUTGOING_FLOW_COOKIE = U64.of(1);
    U64 INCOMING_FLOW_COOKIE = U64.of(2);

    /**
     * Given a session object, build a list of flows that will match on the entire session, and perform any necessary rewrites.
     *
     * @param session the session defines the matches and rewrite actions
     * @return a list of flows that can be inserted
     */
    List<OFMessage> buildFlows(Session session);

    List<OFMessage> buildFlows(TarnIPv4Session session);
}
