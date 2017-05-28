package net.floodlightcontroller.randomizer;

import org.projectfloodlight.openflow.types.IPv4Address;

/**
 * Created by geddingsbarrineau on 5/21/17.
 */
public interface ITARNHost extends IHost {

    IPv4Address getRandomizedAddress();

}
