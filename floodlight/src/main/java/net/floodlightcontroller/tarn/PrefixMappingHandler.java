package net.floodlightcontroller.tarn;

import net.floodlightcontroller.packet.IPv4;
import org.projectfloodlight.openflow.types.IPv4Address;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is responsible for maintaining all of the current prefix mappings needed for TARN.
 * <p>
 * Created by @geddings on 11/2/17.
 */
class PrefixMappingHandler {

    private Map<IPv4Address, PrefixMapping> prefixMappings;

    PrefixMappingHandler() {
        prefixMappings = new HashMap<>();
    }

    /**
     * Adds a prefix mapping for a TARN device. If a prefix mapping already exists for that device, the existing mapping
     * will be overwritten by the newly added mapping.
     *
     * @param mapping the prefix mapping to add
     */
    void addMapping(PrefixMapping mapping) {
        prefixMappings.put(mapping.getInternalIp(), mapping);
    }

    void removeMapping(IPv4Address internalIp) {
        prefixMappings.remove(internalIp);
    }

    Optional<PrefixMapping> getMapping(IPv4Address internalIp) {
        return Optional.ofNullable(prefixMappings.get(internalIp));
    }

    Collection<PrefixMapping> getMappings() {
        return prefixMappings.values();
    }

    /**
     * Returns a mapping associated with the given IP address, if it exists. A mapping is associated if the IP address
     * corresponds to an internal IP or is a part of an external prefix range in any of the existing mappings.
     *
     * @param iPv4Address the IP address associated with a prefix mapping
     * @return an optional prefix mapping
     */
    Optional<PrefixMapping> getAssociatedMapping(IPv4Address iPv4Address) {
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

    /**
     * Returns true if the IPv4 packet contains an IP address that is associated with a TARN device, whether internal or
     * external.
     *
     * @param iPv4 the packet in question
     * @return true if the packet is associated with at least one TARN device
     */
    Boolean isTarnDevice(IPv4 iPv4) {
        return containsInternalIp(iPv4) || containsExternalIp(iPv4);
    }

    Boolean isInternalIp(IPv4Address iPv4Address) {
        return prefixMappings.containsKey(iPv4Address);
    }

    Boolean isExternalIp(IPv4Address iPv4Address) {
        return prefixMappings.values().stream()
                .map(PrefixMapping::getCurrentPrefix)
                .anyMatch(prefix -> prefix.contains(iPv4Address));
    }

    Boolean containsInternalIp(IPv4 iPv4) {
        return isInternalIp(iPv4.getSourceAddress()) || isInternalIp(iPv4.getDestinationAddress());
    }

    Boolean containsExternalIp(IPv4 iPv4) {
        return isExternalIp(iPv4.getSourceAddress()) || isExternalIp(iPv4.getDestinationAddress());
    }
}
