package net.floodlightcontroller.tarn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.tarn.web.PrefixMappingSerializer;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;

/**
 * A mapping of an internal IP address with the current external prefix associated with it. These mappings will
 * be agreed upon out-of-band by anyone wanting to use a TARN service.
 *
 * Created by @geddings on 11/2/17.
 */
@JsonSerialize(using = PrefixMappingSerializer.class)
public class PrefixMapping {
    
    private final IPv4Address internalIp;
    private final IPv4AddressWithMask currentPrefix;

    /**
     * Creates a new TARN prefix mapping.
     * @param internalIp the internal IP of the TARN device
     * @param currentPrefix the current external prefix being mapped to the TARN device
     */
    @JsonCreator
    PrefixMapping(@JsonProperty("internal-ip") String internalIp, @JsonProperty("external-prefix") String currentPrefix) {
        this.internalIp = IPv4Address.of(internalIp);
        this.currentPrefix = IPv4AddressWithMask.of(currentPrefix);
    }

    public IPv4Address getInternalIp() {
        return internalIp;
    }

    public IPv4AddressWithMask getCurrentPrefix() {
        return currentPrefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrefixMapping that = (PrefixMapping) o;

        return internalIp != null ? internalIp.equals(that.internalIp) : that.internalIp == null;
    }

    @Override
    public int hashCode() {
        return internalIp != null ? internalIp.hashCode() : 0;
    }
}
