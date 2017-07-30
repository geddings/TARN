package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4AddressWithMask;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 6/30/17.
 */
public class PrefixChangeEvent {

    private AutonomousSystem as;
    private IPv4AddressWithMask oldPrefix;
    private IPv4AddressWithMask newPrefix;

    public PrefixChangeEvent(AutonomousSystem as, IPv4AddressWithMask oldPrefix, IPv4AddressWithMask newPrefix) {
        this.as = as;
        this.oldPrefix = oldPrefix;
        this.newPrefix = newPrefix;
    }

    public AutonomousSystem getAs() {
        return as;
    }

    public IPv4AddressWithMask getOldPrefix() {
        return oldPrefix;
    }

    public IPv4AddressWithMask getNewPrefix() {
        return newPrefix;
    }

    @Override
    public String toString() {
        return "PrefixChangeEvent{" +
                "as=" + as.getASNumber() +
                ", oldPrefix=" + oldPrefix +
                ", newPrefix=" + newPrefix +
                '}';
    }
}
