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

    /**
     * Check if the Randomizer module is enabled.
     *
     * @return True if enabled
     */
    boolean isEnabled();

    /**
     * Enable Randomizer module
     *
     * @return enabled
     */
    RandomizerReturnCode enable();

    /**
     * Disable Randomizer module
     *
     * @return disabled
     */
    RandomizerReturnCode disable();

    /**
     * Check if Floodlight is randomizing hosts.
     *
     * @return True if hosts are randomized, else False
     */
    boolean isRandom();

    RandomizerReturnCode setRandom(Boolean random);

    /**
     * Retrieve the configured local port
     *
     * @return localport
     */
    OFPort getLanPort();

    RandomizerReturnCode setLanPort(int portnumber);

    /**
     * Retrieve the configured wan port
     *
     * @return wanport
     */
    OFPort getWanPort();

    RandomizerReturnCode setWanPort(int portnumber);
    
    RandomizedHost getServer(IPv4Address serveraddress);
    
    List<RandomizedHost> getServers();

    RandomizerReturnCode addServer(RandomizedHost randomizedHost);

    RandomizerReturnCode removeServer(RandomizedHost randomizedHost);

    List<Connection> getConnections();

    RandomizerReturnCode addConnection(Connection connection);

    RandomizerReturnCode removeConnection(Connection connection);

    Map<IPv4Address, IPv4AddressWithMask> getCurrentPrefix();

    Map<IPv4Address, List<IPv4AddressWithMask>> getPrefixes();
    
    void addPrefix(RandomizedHost randomizedHost, IPv4AddressWithMask prefix);
    
    void removePrefix(RandomizedHost randomizedHost, IPv4AddressWithMask prefix);

    enum RandomizerReturnCode {
        WHITELIST_ENTRY_ADDED, WHITELIST_ENTRY_REMOVED,
        ERR_DUPLICATE_WHITELIST_ENTRY, ERR_UNKNOWN_WHITELIST_ENTRY,
        SERVER_ADDED, SERVER_REMOVED,
        ERR_DUPLICATE_SERVER, ERR_UNKNOWN_SERVER,
        CONNECTION_ADDED, CONNECTION_REMOVED,
        ERR_DUPLICATE_CONNECTION, ERR_UNKNOWN_CONNECTION,
        ENABLED, DISABLED,
        CONFIG_SET,
        READY, NOT_READY,
        STATS_CLEARED
    }

}
