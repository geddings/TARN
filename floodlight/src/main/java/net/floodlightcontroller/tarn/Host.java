package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4Address;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by geddingsbarrineau on 8/24/17.
 */
public class Host {
    
    private final IPv4Address internal;
    private IPv4Address external;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public Host(IPv4Address internal) {
        this.internal = internal;
        this.external = IPv4Address.NONE;
    }
    
    
}
