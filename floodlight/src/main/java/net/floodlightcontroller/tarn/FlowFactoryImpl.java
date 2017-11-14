package net.floodlightcontroller.tarn;

import com.google.common.collect.ImmutableList;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.TransportPort;
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
        OFMessage inboundFlow = factory.buildFlowAdd()
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(5)
                .setPriority(100)
                .setMatch(buildMatch(session.getInbound()))
                .setActions(buildActions(session.getInbound(), session.getOutbound()))
                .build();

        /* Build outbound flow */
        OFMessage outboundFlow = factory.buildFlowAdd()
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(5)
                .setPriority(100)
                .setMatch(buildMatch(session.getOutbound()))
                .setActions(buildActions(session.getOutbound(), session.getInbound()))
                .build();

        return ImmutableList.of(inboundFlow, outboundFlow);
    }

    private Match buildMatch(ConnectionAttributes connection) {
        Match.Builder builder = factory.buildMatch()
                .setExact(MatchField.IN_PORT, connection.getInPort())
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
                .setExact(MatchField.IPV4_SRC, connection.getSrcIp())
                .setExact(MatchField.IPV4_DST, connection.getDstIp());

        if (!connection.getSrcPort().equals(TransportPort.NONE)) {
            builder.setExact(MatchField.TCP_SRC, connection.getSrcPort());
        }

        if (!connection.getDstPort().equals(TransportPort.NONE)) {
            builder.setExact(MatchField.TCP_DST, connection.getDstPort());
        }

        return builder.build();
    }

    private List<OFAction> buildActions(ConnectionAttributes connection, ConnectionAttributes oppositeConnection) {
        List<OFAction> actions = new ArrayList<>();
        OFOxms oxms = factory.oxms();

        /* Check if source needs to be rewritten */
        if (!connection.getSrcIp().equals(oppositeConnection.getDstIp())) {
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(oxms.ipv4Src(oppositeConnection.getDstIp()))
                    .build());
        }

        /* Check if destination needs to be rewritten */
        if (!connection.getDstIp().equals(oppositeConnection.getSrcIp())) {
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(oxms.ipv4Dst(oppositeConnection.getSrcIp()))
                    .build());
        }

        /* Set output port */
        actions.add(factory.actions()
                .buildOutput()
                .setMaxLen(0xFFffFFff)
                .setPort(connection.getOutPort())
                .build());

        return actions;
    }
}
