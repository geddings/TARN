package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.types.IPv4Address;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/2/17.
 */
public interface TarnService extends IFloodlightService {

    Collection<PrefixMapping> getPrefixMappings();

    Optional<PrefixMapping> getPrefixMapping(IPv4Address internalIp);

    void addPrefixMapping(PrefixMapping mapping);
    
    void removePrefixMapping(IPv4Address internalIp);

    Collection<TarnSession> getSessions();

    boolean isEnabled();
    
    void setEnable(boolean enable);
}
