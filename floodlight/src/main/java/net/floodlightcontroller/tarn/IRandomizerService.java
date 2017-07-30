package net.floodlightcontroller.tarn;

import java.util.List;

import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.module.IFloodlightService;

/**
 * Created by geddingsbarrineau on 9/19/16.
 *
 */
public interface IRandomizerService extends IFloodlightService {

    void addAutonomousSystem(AutonomousSystem as);

    void addAutonomousSystem(int ASNumber, String internalPrefix);

    void removeAutonomousSystem(int ASNumber);

    List<AutonomousSystem> getAutonomousSystems();
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
