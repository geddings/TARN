package net.floodlightcontroller.tarn.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.floodlightcontroller.tarn.FlowFactory;
import net.floodlightcontroller.tarn.PacketFlow;
import net.floodlightcontroller.tarn.Session;
import net.floodlightcontroller.tarn.types.TransportPacketFlow;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                .setFlags(ImmutableSet.of(OFFlowModFlags.SEND_FLOW_REM))
                .build();
    }

    private Match buildMatch(PacketFlow packetFlow) {
        Match.Builder builder = factory.buildMatch()
                .setExact(MatchField.IN_PORT, packetFlow.getInPort())
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
                .setExact(MatchField.IPV4_SRC, packetFlow.getSrcIp())
                .setExact(MatchField.IPV4_DST, packetFlow.getDstIp());

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
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(oxms.ipv4Src(oppositePacketFlow.getDstIp()))
                    .build());
        }

        /* Check if destination needs to be rewritten */
        if (!packetFlow.getDstIp().equals(oppositePacketFlow.getSrcIp())) {
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(oxms.ipv4Dst(oppositePacketFlow.getSrcIp()))
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
