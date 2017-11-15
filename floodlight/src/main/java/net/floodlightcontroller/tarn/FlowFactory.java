package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.protocol.OFMessage;

import java.util.List;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/2/17.
 */
public interface FlowFactory {

    /**
     * Given a session object, build a list of flows that will match on the entire session, and perform any necessary rewrites.
     *
     * @param session the session defines the matches and rewrite actions
     * @return a list of flows that can be inserted
     */
    @Deprecated
    List<OFMessage> buildFlows(SessionImpl session);
    
    List<OFMessage> buildFlows(Session session);
    
}
