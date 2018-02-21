package net.floodlightcontroller.tarn;

import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.IPv6;
import org.projectfloodlight.openflow.types.IPAddress;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is responsible for maintaining all of the current prefix mappings needed for TARN.
 * <p>
 * Created by @geddings on 11/2/17.
 */
public class PrefixMappingHandler {

    private Map<IPAddress, PrefixMapping> prefixMappings;

    public PrefixMappingHandler() {
        prefixMappings = new HashMap<>();
    }

    /**
     * Adds a prefix mapping for a TARN device. If a prefix mapping already exists for that device, the existing mapping
     * will be overwritten by the newly added mapping.
     *
     * @param mapping the prefix mapping to add
     */
    public void addMapping(PrefixMapping mapping) {
        prefixMappings.put(mapping.getInternalIp(), mapping);
    }

    public void removeMapping(IPAddress internalIp) {
        prefixMappings.remove(internalIp);
    }

    Optional<PrefixMapping> getMapping(IPAddress internalIp) {
        return Optional.ofNullable(prefixMappings.get(internalIp));
    }

    public Collection<PrefixMapping> getMappings() {
        return prefixMappings.values();
    }

    /**
     * Returns a mapping associated with the given IP address, if it exists. A mapping is associated if the IP address
     * corresponds to an internal IP or is a part of an external prefix range in any of the existing mappings.
     *
     * @param ipAddress the IP address associated with a prefix mapping
     * @return an optional prefix mapping
     */
    public Optional<PrefixMapping> getAssociatedMapping(IPAddress ipAddress) {
        if (isInternalIp(ipAddress)) {
            return Optional.of(prefixMappings.get(ipAddress));
        } else if (isExternalIp(ipAddress)) {
            return prefixMappings.values().stream()
                    .filter(mapping -> mapping.getCurrentPrefix().contains(ipAddress))
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
    public Boolean isTarnDevice(IPv4 iPv4) {
        return containsInternalIp(iPv4) || containsExternalIp(iPv4);
    }

    public Boolean isTarnDevice(IPv6 iPv6) {
        return containsInternalIp(iPv6) || containsExternalIp(iPv6);
    }

    public Boolean isInternalIp(IPAddress ipAddress) {
        return prefixMappings.containsKey(ipAddress);
    }

    public Boolean isExternalIp(IPAddress ipAddress) {
        return prefixMappings.values().stream()
                .map(PrefixMapping::getCurrentPrefix)
                .anyMatch(prefix -> prefix.contains(ipAddress));
    }

    public Boolean containsInternalIp(IPv4 iPv4) {
        return isInternalIp(iPv4.getSourceAddress()) || isInternalIp(iPv4.getDestinationAddress());
    }

    Boolean containsExternalIp(IPv4 iPv4) {
        return isExternalIp(iPv4.getSourceAddress()) || isExternalIp(iPv4.getDestinationAddress());
    }

    public Boolean containsInternalIp(IPv6 iPv6) {
        return isInternalIp(iPv6.getSourceAddress()) || isInternalIp(iPv6.getDestinationAddress());
    }

    Boolean containsExternalIp(IPv6 iPv6) {
        return isExternalIp(iPv6.getSourceAddress()) || isExternalIp(iPv6.getDestinationAddress());
    }
}
