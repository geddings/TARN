package net.floodlightcontroller.tarn.internal;

import com.google.common.collect.ImmutableList;
import net.floodlightcontroller.tarn.FlowFactory;
import net.floodlightcontroller.tarn.PacketFlow;
import net.floodlightcontroller.tarn.Session;
import net.floodlightcontroller.tarn.types.TarnIPv4Session;
import net.floodlightcontroller.tarn.types.TransportPacketFlow;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
//        OFMessage inboundFlow = buildFlow(buildMatch(session.getInbound()), buildActions(session.getInbound(),
//                session.getOutbound()));
//
//        /* Build outbound flow */
//        OFMessage outboundFlow = buildFlow(buildMatch(session.getOutbound()), buildActions(session.getOutbound(),
//                session.getInbound()));

//        return ImmutableList.of(inboundFlow, outboundFlow);
        return null;
    }

    @Override
    public List<OFMessage> buildFlows(TarnIPv4Session session) {
        /* Build outgoing flow: internal -> external */
        OFMessage outgoingFlow = buildFlow(buildOutgoingMatch(session), buildOutgoingActions(session), OUTGOING_FLOW_COOKIE);

        /* Build incoming flow: external -> internal */
        OFMessage incomingFlow = buildFlow(buildIncomingMatch(session), buildIncomingActions(session), INCOMING_FLOW_COOKIE);

        return ImmutableList.of(outgoingFlow, incomingFlow);
    }

    private OFMessage buildFlow(Match match, List<OFAction> actions, U64 cookie) {
        return factory.buildFlowAdd()
                .setCookie(cookie)
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(5)
                .setPriority(100)
                .setMatch(match)
                .setActions(actions)
                .build();
    }

    private Match buildIncomingMatch(TarnIPv4Session session) {
        return buildIPv4Match(session.getExternalPort(), session.getExternalSrcIp(), session.getExternalDstIp(), session.getIpProtocol(),
                session.getExternalSrcPort(), session.getExternalDstPort());
    }

    private Match buildOutgoingMatch(TarnIPv4Session session) {
        return buildIPv4Match(session.getInternalPort(), session.getInternalSrcIp(), session.getInternalDstIp(), session.getIpProtocol(),
                session.getInternalSrcPort(), session.getInternalDstPort());
    }

    private Match buildIPv4Match(OFPort inPort, IPv4Address srcIp, IPv4Address dstIp, IpProtocol ipProtocol,
                                 TransportPort srcPort, TransportPort dstPort) {
        Match.Builder builder = factory.buildMatch();

        builder.setExact(MatchField.IN_PORT, inPort)
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
                .setExact(MatchField.IPV4_SRC, srcIp)
                .setExact(MatchField.IPV4_DST, dstIp);

        if (ipProtocol == IpProtocol.TCP) {
            builder.setExact(MatchField.IP_PROTO, IpProtocol.TCP);
            builder.setExact(MatchField.TCP_SRC, srcPort);
            builder.setExact(MatchField.TCP_DST, dstPort);
        } else if (ipProtocol == IpProtocol.UDP) {
            builder.setExact(MatchField.IP_PROTO, IpProtocol.UDP);
            builder.setExact(MatchField.UDP_SRC, srcPort);
            builder.setExact(MatchField.UDP_DST, dstPort);
        }

        return builder.build();
    }

    public List<OFAction> buildIncomingActions(TarnIPv4Session session) {
        return buildIPv4Actions(session.getExternalSrcIp(), session.getExternalDstIp(), session.getInternalSrcIp(),
                session.getInternalDstIp(), session.getInternalPort());
    }

    public List<OFAction> buildOutgoingActions(TarnIPv4Session session) {
        return buildIPv4Actions(session.getInternalSrcIp(), session.getInternalDstIp(), session.getExternalSrcIp(),
                session.getExternalDstIp(), session.getExternalPort());
    }

    private List<OFAction> buildIPv4Actions(IPv4Address srcBefore, IPv4Address dstBefore, IPv4Address srcAfter,
                                            IPv4Address dstAfter, OFPort outPort) {
        List<OFAction> actions = new ArrayList<>();
        OFOxms oxms = factory.oxms();

        /* Check if source needs to be rewritten */
        if (!srcBefore.equals(srcAfter)) {
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(oxms.ipv4Src(srcAfter))
                    .build());
        }

        /* Check if destination needs to be rewritten */
        if (!dstBefore.equals(dstAfter)) {
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(oxms.ipv4Dst(dstAfter))
                    .build());
        }

        /* Set output port */
        actions.add(factory.actions()
                .buildOutput()
                .setMaxLen(0xFFffFFff)
                .setPort(outPort)
                .build());

        return actions;
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
