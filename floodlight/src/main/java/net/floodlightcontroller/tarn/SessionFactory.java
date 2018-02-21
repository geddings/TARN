package net.floodlightcontroller.tarn;

import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.IPv6;
import org.projectfloodlight.openflow.types.OFPort;

/**
 * Created by @geddings on 11/15/17.
 */
public interface SessionFactory {
    Session getSession(OFPort inPort, OFPort outPort, IPv4 ipv4);

    Session getSession(OFPort inPort, OFPort outPort, IPv6 ipv6);
}
