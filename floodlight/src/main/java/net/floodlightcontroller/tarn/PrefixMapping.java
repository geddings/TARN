package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;

/**
 * Created by @geddings on 11/2/17.
 */
public class PrefixMapping {
    
    private final IPv4Address internalIp;
    private final IPv4AddressWithMask currentPrefix;

    public PrefixMapping(IPv4Address internalIp, IPv4AddressWithMask currentPrefix) {
        this.internalIp = internalIp;
        this.currentPrefix = currentPrefix;
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
