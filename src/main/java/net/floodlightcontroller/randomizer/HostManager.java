package net.floodlightcontroller.randomizer;

import org.projectfloodlight.openflow.types.IPv4Address;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geddingsbarrineau on 9/14/16.
 * <p>
 * This is the server manager for the EAGER project.
 */
public class HostManager {

    private List<RandomizedHost> randomizedHosts;

    HostManager() {
        randomizedHosts = new ArrayList<>();
    }

    public List<RandomizedHost> getHosts() {
        return randomizedHosts;
    }

    public void updateHosts() {
        randomizedHosts.forEach(RandomizedHost::update);
    }

    public void addHost(RandomizedHost host) {
        randomizedHosts.add(host);
    }

    public void removeHost(RandomizedHost host) {
        randomizedHosts.remove(host);
    }

    public RandomizedHost getServerFromAddress(IPv4Address ip) {
        return randomizedHosts.stream()
                .filter(p -> ip.equals(p.getAddress(IHost.AddressType.INTERNAL)))
                .findAny()
                .orElse(null);
    }

    public RandomizedHost getServerFromRandomizedAddress(IPv4Address ip) {
        return randomizedHosts.stream()
                .filter(p -> ip.equals(p.getRandomizedAddress()))
                .findAny()
                .orElse(null);
    }

    public RandomizedHost getServer(IPv4Address ip) {
        RandomizedHost randomizedHost;
        
        if ((randomizedHost = getServerFromAddress(ip)) != null) return randomizedHost;
        else if ((randomizedHost = getServerFromRandomizedAddress(ip)) != null) return randomizedHost;
        else return null;
    }

}
