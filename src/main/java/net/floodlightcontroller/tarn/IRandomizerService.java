package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.List;
import java.util.Map;

/**
 * Created by geddingsbarrineau on 9/19/16.
 *
 */
public interface IRandomizerService extends IFloodlightService {

    void addASNetwork(ASNetwork asNetwork);

    void addASNetwork(int ASNumber, IPv4AddressWithMask internalPrefix);

    void removeASNetwork(int ASNumber);

    /**
     * Retrieve the configured local port
     *
     * @return localport
     */
    OFPort getLanPort();

    void setLanPort(int portnumber);

    /**
     * Retrieve the configured wan port
     *
     * @return wanport
     */
    OFPort getWanPort();

    void setWanPort(int portnumber);

}
