package net.floodlightcontroller.randomizer;

import org.projectfloodlight.openflow.types.IPv4Address;

/**
 * Created by geddingsbarrineau on 4/7/17.
 */
public class Host implements IHost {

    private IPv4Address address;

    public Host(IPv4Address address) {
        this.address = address;
    }

    public IPv4Address getAddress() {
        return address;
    }

    @Override
    public IPv4Address getAddress(AddressType type) {
        if (type == AddressType.INTERNAL) return address;
        else return null;
    }

    @Override
    public String toString() {
        return "Host{" +
                "address=" + address +
                '}';
    }
}
