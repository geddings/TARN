package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4Address;

/**
 * Created by geddingsbarrineau on 5/21/17.
 */
public interface INormalHost extends IHost {

    IPv4Address getAddress();

}
