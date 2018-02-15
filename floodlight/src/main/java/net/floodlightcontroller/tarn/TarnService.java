package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.module.IFloodlightService;

import java.util.Collection;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/2/17.
 */
public interface TarnService extends IFloodlightService {

    Collection<PrefixMapping> getPrefixMappings();

    void addPrefixMapping(PrefixMapping mapping);

    Collection<Session> getSessions();

    boolean isEnabled();
    
    void setEnable(boolean enable);
}
