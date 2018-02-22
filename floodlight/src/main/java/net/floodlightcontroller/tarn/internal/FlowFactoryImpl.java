package net.floodlightcontroller.tarn.internal;

import java.util.ArrayList;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPVersion;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import net.floodlightcontroller.tarn.FlowFactory;
import net.floodlightcontroller.tarn.PacketFlow;
import net.floodlightcontroller.tarn.Session;
import net.floodlightcontroller.tarn.types.TransportPacketFlow;

/**
 * The Flow Factory is intended to take all responsibility for creating
 * the correct matches and actions for all the different types of flows
 * needed for the Randomizer.
 * <p>
 * With the new session-based approach, this class will no longer be responsible for writing the flows out to a switch.
 * It will be completely stateless, and will only generate flows for different object, like a Session.
 * <p>
 * Created by geddingsbarrineau on 12/13/16.
 */
public class FlowFactoryImpl implements FlowFactory {
    private static final Logger log = LoggerFactory.getLogger(FlowFactoryImpl.class);

    /* We have to use OF15 in order to rewrite prefixes */
    private final OFFactory factory = OFFactories.getFactory(OFVersion.OF_15);

    @Override
    public List<OFMessage> buildFlows(Session session) {
        /* Build inbound flow */
        OFMessage inboundFlow = buildFlow(buildMatch(session.getInbound()), buildActions(session.getInbound(), session.getOutbound()));

        /* Build outbound flow */
        OFMessage outboundFlow = buildFlow(buildMatch(session.getOutbound()), buildActions(session.getOutbound(), session.getInbound()));

        return ImmutableList.of(inboundFlow, outboundFlow);
    }

    private OFMessage buildFlow(Match match, List<OFAction> actions) {
        return factory.buildFlowAdd()
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(5)
                .setPriority(100)
                .setMatch(match)
                .setActions(actions)
                .build();
    }

    private Match buildMatch(PacketFlow packetFlow) {
        Match.Builder builder = factory.buildMatch()
                .setExact(MatchField.IN_PORT, packetFlow.getInPort());

        if (packetFlow.getIpVersion().equals(IPVersion.IPv4)) {
            builder.setExact(MatchField.ETH_TYPE, EthType.IPv4)
                    .setExact(MatchField.IPV4_SRC, (IPv4Address) packetFlow.getSrcIp())
                    .setExact(MatchField.IPV4_DST, (IPv4Address) packetFlow.getDstIp());
        } else {
            builder.setExact(MatchField.ETH_TYPE, EthType.IPv6)
                    .setExact(MatchField.IPV6_SRC, (IPv6Address) packetFlow.getSrcIp())
                    .setExact(MatchField.IPV6_DST, (IPv6Address) packetFlow.getDstIp());
        }

        if (packetFlow instanceof TransportPacketFlow) {
            TransportPacketFlow transportPacketFlow = (TransportPacketFlow) packetFlow;
            if (transportPacketFlow.getIpProtocol().equals(IpProtocol.TCP)) {
                builder.setExact(MatchField.IP_PROTO, IpProtocol.TCP);
                builder.setExact(MatchField.TCP_SRC, transportPacketFlow.getSrcPort());
                builder.setExact(MatchField.TCP_DST, transportPacketFlow.getDstPort());
            } else if (transportPacketFlow.getIpProtocol().equals(IpProtocol.UDP)) {
                builder.setExact(MatchField.IP_PROTO, IpProtocol.UDP);
                builder.setExact(MatchField.UDP_SRC, transportPacketFlow.getSrcPort());
                builder.setExact(MatchField.UDP_DST, transportPacketFlow.getDstPort());
            }
        }

        return builder.build();
    }

    private List<OFAction> buildActions(PacketFlow packetFlow, PacketFlow oppositePacketFlow) {
        List<OFAction> actions = new ArrayList<>();
        OFOxms oxms = factory.oxms();

        /* Check if source needs to be rewritten */
        if (!packetFlow.getSrcIp().equals(oppositePacketFlow.getDstIp())) {
            OFOxm srcOxm = packetFlow.getIpVersion() == IPVersion.IPv4
                    ? oxms.ipv4Src((IPv4Address) oppositePacketFlow.getDstIp())
                    : oxms.ipv6Src((IPv6Address) oppositePacketFlow.getDstIp());
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(srcOxm)
                    .build());
        }

        /* Check if destination needs to be rewritten */
        if (!packetFlow.getDstIp().equals(oppositePacketFlow.getSrcIp())) {
            OFOxm dstOxm = packetFlow.getIpVersion() == IPVersion.IPv4
                    ? oxms.ipv4Dst((IPv4Address) oppositePacketFlow.getSrcIp())
                    : oxms.ipv6Dst((IPv6Address) oppositePacketFlow.getSrcIp());
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(dstOxm)
                    .build());
        }

        /* Set output port */
        actions.add(factory.actions()
                .buildOutput()
                .setMaxLen(0xFFffFFff)
                .setPort(packetFlow.getOutPort())
                .build());

        return actions;
    }
}
