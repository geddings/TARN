package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;

import java.util.List;

/**
 * Created by geddingsbarrineau on 5/21/17.
 */
public interface IFlowFactory {

    List<OFFlowMod> getFlows(OFFlowModCommand fmc);

}
