package net.floodlightcontroller.randomizer;

import org.projectfloodlight.openflow.types.IPv4Address;

/**
 * Created by geddingsbarrineau on 5/21/17.
 */
public interface IHost {

    IPv4Address getAddress(AddressType type);

    enum AddressType {
        INTERNAL, EXTERNAL
    }
}
