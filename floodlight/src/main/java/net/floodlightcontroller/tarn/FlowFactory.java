package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.tarn.flows.RewriteFlow;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
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
public class FlowFactory {
    private static final Logger log = LoggerFactory.getLogger(FlowFactory.class);

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private static DatapathId sw = DatapathId.NONE;
    private static IOFSwitchService switchService = null;

    private static OFPort WAN_PORT = OFPort.of(2);
    private static OFPort LAN_PORT = OFPort.of(1);

    private static Boolean default_flows_set = false;

    public static void setSwitch(DatapathId sw) {
        FlowFactory.sw = sw;
    }

    public static void setSwitchService(IOFSwitchService switchService) {
        FlowFactory.switchService = switchService;
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
        flows.add(RewriteFlow.builder().as(as).ethType(EthType.ARP).source().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().as(as).ethType(EthType.ARP).destination().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().as(as).ethType(EthType.ARP).source().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().as(as).ethType(EthType.ARP).destination().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
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
        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.ARP).source().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.ARP).destination().encrypt().inPort(LAN_PORT).outPort(WAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.ARP).source().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
        flows.add(RewriteFlow.builder().host(host).as(as).ethType(EthType.ARP).destination().decrypt().inPort(WAN_PORT).outPort(LAN_PORT).build().getFlow());
        return flows;
    }
}
