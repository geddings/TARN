package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.List;
import java.util.Optional;

/**
 * Created by geddingsbarrineau on 9/19/16.
 *
 */
@Deprecated
public interface IRandomizerService extends IFloodlightService {

    void addAutonomousSystem(AutonomousSystem as);

    void addAutonomousSystem(int ASNumber, String internalPrefix);

    void removeAutonomousSystem(int ASNumber);

    List<AutonomousSystem> getAutonomousSystems();

    Optional<AutonomousSystem> getAutonomousSystem(int asNumber);
    
    void addHost(Host host);
    
    void removeHost(Host host);
    
    List<Host> getHosts();

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
