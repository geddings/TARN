package net.floodlightcontroller.tarn.web;

import com.google.common.collect.ImmutableList;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.tarn.PrefixMapping;
import net.floodlightcontroller.tarn.TarnService;
import net.floodlightcontroller.tarn.TarnSession;
import net.floodlightcontroller.tarn.types.TarnIPv4Session;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFPort;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/26/18.
 */
public class SessionsResource extends ServerResource {

    @Get
    public Object getSessions() {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());
        Collection<TarnSession> sessions = tarnService.getSessions();

        String status = getQueryValue("status");
        Predicate<TarnSession> statusPredicate = s -> true;
        if (status != null) {
            if (status.equals("active")) {
                statusPredicate = s -> s.getStatus().equals(TarnSession.Status.ACTIVE);
            } else if (status.equals("inactive")) {
                statusPredicate = s -> s.getStatus().equals(TarnSession.Status.INACTIVE);
            }
        }

        String protocol = getQueryValue("ip-protocol");
        Predicate<TarnSession> protocolPredicate = s -> true;
        if (protocol != null) {
            if (protocol.equals("icmp")) {
                protocolPredicate = s -> s.getIpProtocol().equals(IpProtocol.ICMP);

            } else if (protocol.equals("tcp")) {
                protocolPredicate = s -> s.getIpProtocol().equals(IpProtocol.TCP);

            } else if (protocol.equals("udp")) {
                protocolPredicate = s -> s.getIpProtocol().equals(IpProtocol.UDP);

            }
        }

        String direction = getQueryValue("direction");
        Predicate<TarnSession> directionPredicate = s -> true;
        if (direction != null) {
            if (direction.equals("incoming")) {
                directionPredicate = s -> s.getDirection().equals(TarnSession.Direction.INCOMING);
            } else if (direction.equals("outgoing")) {
                directionPredicate = s -> s.getDirection().equals(TarnSession.Direction.OUTGOING);
            }
        }

        return sessions.stream()
                .filter(statusPredicate)
                .filter(protocolPredicate)
                .filter(directionPredicate)
                .collect(Collectors.toList());
    }
}
