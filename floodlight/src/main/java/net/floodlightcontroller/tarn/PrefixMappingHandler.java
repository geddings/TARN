package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4Address;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by @geddings on 11/2/17.
 */
public class PrefixMappingHandler {
    
    private Map<IPv4Address, PrefixMapping> prefixMappings;

    public PrefixMappingHandler() {
        prefixMappings = new HashMap<>();
    }
    
    public void add(PrefixMapping mapping) {
        prefixMappings.put(mapping.getInternalIp(), mapping);
    }
    
    public void remove(IPv4Address internalIp) {
        prefixMappings.remove(internalIp);
    }
    
    public Optional<PrefixMapping> get(IPv4Address internalIp) {
        return Optional.ofNullable(prefixMappings.get(internalIp));
    }
    
    public Boolean isTarnDevice(IPv4Address iPv4Address) {
        
        if (prefixMappings.containsKey(iPv4Address)) {
            return true;
        }
        
        for (PrefixMapping mapping : prefixMappings.values()) {
            if (mapping.getCurrentPrefix().contains(iPv4Address)) {
                return true;
            }
        }
        
        return false;
    }
}
