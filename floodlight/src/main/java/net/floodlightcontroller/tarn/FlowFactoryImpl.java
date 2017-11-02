package net.floodlightcontroller.tarn;

import com.google.common.collect.ImmutableList;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.tarn.flows.RewriteFlow;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Flow Factory is intended to take all responsibility for creating
 * the correct matches and actions for all the different types of flows
 * needed for the Randomizer.
 * <p>
 * Created by geddingsbarrineau on 12/13/16.
 */
public class FlowFactoryImpl implements FlowFactory {
    private static final Logger log = LoggerFactory.getLogger(FlowFactoryImpl.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static DatapathId sw = DatapathId.NONE;
    private static IOFSwitchService switchService = null;

    /* We have to use OF15 in order to rewrite prefixes */
    private final OFFactory factory = OFFactories.getFactory(OFVersion.OF_15);

    private static OFPort WAN_PORT = OFPort.of(2);
    private static OFPort LAN_PORT = OFPort.of(1);

    private static Boolean default_flows_set = false;

    @Override
    public List<OFMessage> buildFlows(Session session) {

        /* Build inbound flow */
        OFMessage inboundFlow = factory.buildFlowAdd()
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(10)
                .setPriority(100)
                .setMatch(buildMatch(session.getInbound()))
                .setActions(buildActions(session.getInbound(), session.getOutbound()))
                .build();

        /* Build outbound flow */
        OFMessage outboundFlow = factory.buildFlowAdd()
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(0)
                .setIdleTimeout(10)
                .setPriority(100)
                .setMatch(buildMatch(session.getOutbound()))
                .setActions(buildActions(session.getOutbound(), session.getInbound()))
                .build();

        return ImmutableList.of(inboundFlow, outboundFlow);
    }

    private Match buildMatch(ConnectionAttributes connection) {
        return factory.buildMatch()
                .setExact(MatchField.IN_PORT, connection.getInPort())
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
                .setExact(MatchField.IPV4_SRC, connection.getSrcIp())
                .setExact(MatchField.IPV4_DST, connection.getDstIp())
                .setExact(MatchField.TCP_SRC, connection.getSrcPort())
                .setExact(MatchField.TCP_DST, connection.getDstPort())
                .build();
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

    public static void setSwitch(DatapathId sw) {
        FlowFactoryImpl.sw = sw;
    }

    public static void setSwitchService(IOFSwitchService switchService) {
        FlowFactoryImpl.switchService = switchService;
    }

    static void setLanPort(int portNumber) {
        try {
            LAN_PORT = OFPort.of(portNumber);
            default_flows_set = false;
        } catch (IllegalArgumentException e) {
            log.error("Illegal port number. Could not set LAN port.");
        }
    }

    static void setWanPort(int portNumber) {
        try {
            WAN_PORT = OFPort.of(portNumber);
            default_flows_set = false;
        } catch (IllegalArgumentException e) {
            log.error("Illegal port number. Could not set WAN port.");
        }
    }

    static void insertDefaultFlows() {
        if (sw.equals(DatapathId.NONE)) {
            log.error("Unable to insert default flows. Switch DPID is not yet configured. Is it connected?");
            return;
        }
        if (switchService == null) {
            log.error("Unable to insert default flows. Switch service is not yet configured.");
            return;
        }

        IOFSwitch iofSwitch = switchService.getActiveSwitch(sw);
        if (iofSwitch != null) {
            List<OFMessage> flows = new ArrayList<>();
            flows.add(iofSwitch.getOFFactory().buildFlowAdd()
                    .setTableId(TableId.of(0))
                    .setPriority(0)
                    .setInstructions(Collections.singletonList(iofSwitch.getOFFactory().instructions()
                            .buildGotoTable().setTableId(TableId.of(1)).build()))
                    .build());
            flows.add(iofSwitch.getOFFactory().buildFlowAdd()
                    .setTableId(TableId.of(1))
                    .setPriority(1)
                    .setMatch(iofSwitch.getOFFactory().buildMatch().setExact(MatchField.IN_PORT, LAN_PORT).build())
                    .setOutPort(WAN_PORT)
                    .build());
            flows.add(iofSwitch.getOFFactory().buildFlowAdd()
                    .setTableId(TableId.of(1))
                    .setPriority(1)
                    .setMatch(iofSwitch.getOFFactory().buildMatch().setExact(MatchField.IN_PORT, WAN_PORT).build())
                    .setOutPort(LAN_PORT)
                    .build());
            iofSwitch.write(flows);
        } else {
            log.error("Switch object not found. Prefix flows will not be inserted");
        }
    }

    private static List<OFInstruction> getInstructions() {
        List<OFInstruction> instructions = new ArrayList<>();
        instructions.add(OFFactories.getFactory(OFVersion.OF_15).instructions().buildGotoTable().setTableId(TableId.of(1)).build());
        return instructions;
    }

    static void insertASRewriteFlows(AutonomousSystem as) {
        Runnable task = () -> {
            if (sw.equals(DatapathId.NONE)) {
                log.error("Unable to insert rewrite flows. Switch DPID is not yet configured. Is it connected?");
                return;
            }
            if (switchService == null) {
                log.error("Unable to insert rewrite flows. Switch service is not yet configured.");
                return;
            }

            if (!default_flows_set) insertDefaultFlows();

            IOFSwitch iofSwitch = switchService.getActiveSwitch(sw);
            if (iofSwitch != null) {
                List<OFMessage> flows = getASFlows(as);
                log.trace("{}", flows);
                iofSwitch.write(flows);
            } else {
                log.error("Switch object not found. Prefix flows will not be inserted");
            }
        };

        executorService.execute(task);
    }

    private static List<OFMessage> getASFlows(AutonomousSystem as) {
        List<OFMessage> flows = new ArrayList<>();
        flows.add(RewriteFlow.builder().as(as).ethType(EthType.IPv4).source().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().as(as).ethType(EthType.IPv4).destination().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().as(as).ethType(EthType.IPv4).source().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().as(as).ethType(EthType.IPv4).destination().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
//        flows.add(RewriteFlow.builder().as(as).ethType(EthType.ARP).source().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
//        flows.add(RewriteFlow.builder().as(as).ethType(EthType.ARP).destination().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
//        flows.add(RewriteFlow.builder().as(as).ethType(EthType.ARP).source().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
//        flows.add(RewriteFlow.builder().as(as).ethType(EthType.ARP).destination().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
        return flows;
    }

    static void insertHostRewriteFlows(Host host, AutonomousSystem as) {
        Runnable task = () -> {
            if (sw.equals(DatapathId.NONE)) {
                log.error("Unable to insert rewrite flows. Switch DPID is not yet configured. Is it connected?");
                return;
            }
            if (switchService == null) {
                log.error("Unable to insert rewrite flows. Switch service is not yet configured.");
                return;
            }

            if (!default_flows_set) insertDefaultFlows();

            IOFSwitch iofSwitch = switchService.getActiveSwitch(sw);
            if (iofSwitch != null) {
                List<OFMessage> flows = getHostFlows(host, as);
                log.trace("{}", flows);
                iofSwitch.write(flows);
            } else {
                log.error("Switch object not found. Host rewrite flows will not be inserted");
            }
        };

        executorService.execute(task);
    }

    private static List<OFMessage> getHostFlows(Host host, AutonomousSystem as) {
        List<OFMessage> flows = new ArrayList<>();
        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.IPv4).source().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.IPv4).destination().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.IPv4).source().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.IPv4).destination().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
//        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.ARP).source().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
//        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.ARP).destination().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
//        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.ARP).source().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
//        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.ARP).destination().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
        return flows;
    }
}
