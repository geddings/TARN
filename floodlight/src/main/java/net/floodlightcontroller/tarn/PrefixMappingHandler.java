package net.floodlightcontroller.tarn;

import net.floodlightcontroller.packet.IPv4;
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

    public void addMapping(PrefixMapping mapping) {
        prefixMappings.put(mapping.getInternalIp(), mapping);
    }

    public void removeMapping(IPv4Address internalIp) {
        prefixMappings.remove(internalIp);
    }

    public Optional<PrefixMapping> getMapping(IPv4Address internalIp) {
        return Optional.ofNullable(prefixMappings.get(internalIp));
    }

    public Optional<PrefixMapping> getAssociatedMapping(IPv4Address iPv4Address) {
        if (isInternalIp(iPv4Address)) {
            return Optional.of(prefixMappings.get(iPv4Address));
        } else if (isExternalIp(iPv4Address)) {
            return prefixMappings.values().stream()
                    .filter(mapping -> mapping.getCurrentPrefix().contains(iPv4Address))
                    .findAny();
        } else {
            return Optional.empty();
        }
    }

    public Boolean isTarnDevice(IPv4 iPv4) {
        return containsInternalIp(iPv4) || containsExternalIp(iPv4);
    }

    public Boolean isInternalIp(IPv4Address iPv4Address) {
        return prefixMappings.containsKey(iPv4Address);
    }

    public Boolean isExternalIp(IPv4Address iPv4Address) {
        return prefixMappings.values().stream()
                .map(PrefixMapping::getCurrentPrefix)
                .anyMatch(prefix -> prefix.contains(iPv4Address));
    }

    public Boolean containsInternalIp(IPv4 iPv4) {
        return isInternalIp(iPv4.getSourceAddress()) || isInternalIp(iPv4.getDestinationAddress());
    }

    public Boolean containsExternalIp(IPv4 iPv4) {
        return isExternalIp(iPv4.getSourceAddress()) || isExternalIp(iPv4.getDestinationAddress());
    }
}
