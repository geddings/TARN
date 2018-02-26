package net.floodlightcontroller.tarn.internal;

import com.google.common.collect.ImmutableList;
import net.floodlightcontroller.tarn.FlowFactory;
import net.floodlightcontroller.tarn.types.TarnIPv4Session;
import net.floodlightcontroller.tarn.types.TarnIPv6Session;
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
    public List<OFMessage> buildFlows(TarnIPv4Session session) {
        /* Build outgoing flow: internal -> external */
        OFMessage outgoingFlow = buildFlow(buildOutgoingMatch(session), buildOutgoingActions(session),
                OUTGOING_FLOW_COOKIE);

        /* Build incoming flow: external -> internal */
        OFMessage incomingFlow = buildFlow(buildIncomingMatch(session), buildIncomingActions(session),
                INCOMING_FLOW_COOKIE);

        return ImmutableList.of(outgoingFlow, incomingFlow);
    }

    @Override
    public List<OFMessage> buildFlows(TarnIPv6Session session) {
        /* Build outgoing flow: internal -> external */
        OFMessage outgoingFlow = buildFlow(buildOutgoingMatch(session), buildOutgoingActions(session),
                OUTGOING_FLOW_COOKIE);

        /* Build incoming flow: external -> internal */
        OFMessage incomingFlow = buildFlow(buildIncomingMatch(session), buildIncomingActions(session),
                INCOMING_FLOW_COOKIE);

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
        return buildMatch(session.getExternalPort(), session.getExternalSrcIp(), session.getExternalDstIp(),
                session.getIpProtocol(),
                session.getExternalSrcPort(), session.getExternalDstPort());
    }

    private Match buildOutgoingMatch(TarnIPv4Session session) {
        return buildMatch(session.getInternalPort(), session.getInternalSrcIp(), session.getInternalDstIp(),
                session.getIpProtocol(),
                session.getInternalSrcPort(), session.getInternalDstPort());
    }

    private Match buildIncomingMatch(TarnIPv6Session session) {
        return buildMatch(session.getExternalPort(), session.getExternalSrcIp(), session.getExternalDstIp(),
                session.getIpProtocol(),
                session.getExternalSrcPort(), session.getExternalDstPort());
    }

    private Match buildOutgoingMatch(TarnIPv6Session session) {
        return buildMatch(session.getInternalPort(), session.getInternalSrcIp(), session.getInternalDstIp(),
                session.getIpProtocol(),
                session.getInternalSrcPort(), session.getInternalDstPort());
    }

    private Match buildMatch(OFPort inPort, IPv4Address srcIp, IPv4Address dstIp, IpProtocol ipProtocol,
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

    private Match buildMatch(OFPort inPort, IPv6Address srcIp, IPv6Address dstIp, IpProtocol ipProtocol,
                             TransportPort srcPort, TransportPort dstPort) {
        Match.Builder builder = factory.buildMatch();

        builder.setExact(MatchField.IN_PORT, inPort)
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
                .setExact(MatchField.IPV6_SRC, srcIp)
                .setExact(MatchField.IPV6_DST, dstIp);

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
        return buildActions(session.getExternalSrcIp(), session.getExternalDstIp(), session.getInternalSrcIp(),
                session.getInternalDstIp(), session.getInternalPort());
    }

    public List<OFAction> buildOutgoingActions(TarnIPv4Session session) {
        return buildActions(session.getInternalSrcIp(), session.getInternalDstIp(), session.getExternalSrcIp(),
                session.getExternalDstIp(), session.getExternalPort());
    }

    public List<OFAction> buildIncomingActions(TarnIPv6Session session) {
        return buildActions(session.getExternalSrcIp(), session.getExternalDstIp(), session.getInternalSrcIp(),
                session.getInternalDstIp(), session.getInternalPort());
    }

    public List<OFAction> buildOutgoingActions(TarnIPv6Session session) {
        return buildActions(session.getInternalSrcIp(), session.getInternalDstIp(), session.getExternalSrcIp(),
                session.getExternalDstIp(), session.getExternalPort());
    }

    private List<OFAction> buildActions(IPv4Address srcBefore, IPv4Address dstBefore, IPv4Address srcAfter,
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

    private List<OFAction> buildActions(IPv6Address srcBefore, IPv6Address dstBefore, IPv6Address srcAfter,
                                        IPv6Address dstAfter, OFPort outPort) {
        List<OFAction> actions = new ArrayList<>();
        OFOxms oxms = factory.oxms();

        /* Check if source needs to be rewritten */
        if (!srcBefore.equals(srcAfter)) {
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(oxms.ipv6Src(srcAfter))
                    .build());
        }

        /* Check if destination needs to be rewritten */
        if (!dstBefore.equals(dstAfter)) {
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(oxms.ipv6Dst(dstAfter))
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
}
