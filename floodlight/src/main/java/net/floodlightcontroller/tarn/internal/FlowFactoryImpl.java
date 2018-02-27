package net.floodlightcontroller.tarn.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.floodlightcontroller.tarn.FlowFactory;
import net.floodlightcontroller.tarn.TarnSession;
import net.floodlightcontroller.tarn.types.TarnIPv4Session;
import net.floodlightcontroller.tarn.types.TarnIPv6Session;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
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
    public List<OFMessage> buildFlows(TarnSession session) {
        /* Build outgoing flow: internal -> external */
        U64 cookie1 = U64.of(session.getId().getLeastSignificantBits());
        OFMessage outgoingFlow = buildFlow(buildOutgoingMatch(session), buildOutgoingActions(session), cookie1);

        /* Build incoming flow: external -> internal */
        U64 cookie2 = U64.of(session.getId().getLeastSignificantBits());
        OFMessage incomingFlow = buildFlow(buildIncomingMatch(session), buildIncomingActions(session), cookie2);

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
//                .setFlags(ImmutableSet.of(OFFlowModFlags.SEND_FLOW_REM))
                .build();
    }

    private Match buildIncomingMatch(TarnSession session) {
        return buildMatch(session.getExternalPort(), session.getExternalSrcIp(), session.getExternalDstIp(),
                session.getIpProtocol(),
                session.getExternalSrcPort(), session.getExternalDstPort());
    }

    private Match buildOutgoingMatch(TarnSession session) {
        return buildMatch(session.getInternalPort(), session.getInternalSrcIp(), session.getInternalDstIp(),
                session.getIpProtocol(),
                session.getInternalSrcPort(), session.getInternalDstPort());
    }

    private Match buildMatch(OFPort inPort, IPAddress srcIp, IPAddress dstIp, IpProtocol ipProtocol,
                             TransportPort srcPort, TransportPort dstPort) {
        Match.Builder builder = factory.buildMatch();

        builder.setExact(MatchField.IN_PORT, inPort);

        if (srcIp.getIpVersion() == IPVersion.IPv4) builder.setExact(MatchField.ETH_TYPE, EthType.IPv4);
        else if (srcIp.getIpVersion() == IPVersion.IPv6) builder.setExact(MatchField.ETH_TYPE, EthType.IPv6);

        if (srcIp.getIpVersion() == IPVersion.IPv4) builder.setExact(MatchField.IPV4_SRC, (IPv4Address) srcIp);
        else if (srcIp.getIpVersion() == IPVersion.IPv6) builder.setExact(MatchField.IPV6_SRC, (IPv6Address) srcIp);

        if (dstIp.getIpVersion() == IPVersion.IPv4) builder.setExact(MatchField.IPV4_DST, (IPv4Address) dstIp);
        else if (dstIp.getIpVersion() == IPVersion.IPv6) builder.setExact(MatchField.IPV6_DST, (IPv6Address) dstIp);

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


    public List<OFAction> buildIncomingActions(TarnSession session) {
        return buildActions(session.getExternalSrcIp(), session.getExternalDstIp(), session.getInternalSrcIp(),
                session.getInternalDstIp(), session.getInternalPort());
    }

    public List<OFAction> buildOutgoingActions(TarnSession session) {
        return buildActions(session.getInternalSrcIp(), session.getInternalDstIp(), session.getExternalSrcIp(),
                session.getExternalDstIp(), session.getExternalPort());
    }

    private List<OFAction> buildActions(IPAddress srcBefore, IPAddress dstBefore, IPAddress srcAfter,
                                        IPAddress dstAfter, OFPort outPort) {
        List<OFAction> actions = new ArrayList<>();

        /* Check if source needs to be rewritten */
        if (!srcBefore.equals(dstAfter)) {
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(getSrcOxm(dstAfter))
                    .build());
        }

        /* Check if destination needs to be rewritten */
        if (!dstBefore.equals(srcAfter)) {
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(getDstOxm(srcAfter))
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

    private OFOxm getSrcOxm(IPAddress ipAddress) {
        OFOxms oxms = factory.oxms();
        return ipAddress.getIpVersion() == IPVersion.IPv4
                ? oxms.ipv4Src((IPv4Address) ipAddress)
                : oxms.ipv6Src((IPv6Address) ipAddress);
    }

    private OFOxm getDstOxm(IPAddress ipAddress) {
        OFOxms oxms = factory.oxms();
        return ipAddress.getIpVersion() == IPVersion.IPv4
                ? oxms.ipv4Dst((IPv4Address) ipAddress)
                : oxms.ipv6Dst((IPv6Address) ipAddress);
    }
}
