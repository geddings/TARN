package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.protocol.OFMessage;

import java.util.List;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/2/17.
 */
public interface FlowFactory {

    List<OFMessage> buildFlows(Session session);

}
