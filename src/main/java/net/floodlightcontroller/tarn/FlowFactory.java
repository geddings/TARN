package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;

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

    private static final TableId SOURCE_REWRITE_TABLE = TableId.of(0);
    private static final TableId DESTINATION_REWRITE_TABLE = TableId.of(1);
    private static final TableId FINAL_TABLE = TableId.of(1);
    private static final OFPort WAN_PORT = OFPort.of(1);
    private static final OFPort LAN_PORT = OFPort.of(2);
    private static final int HARD_TIMEOUT = 30;
    private static final int IDLE_TIMEOUT = 30;
    private static final int FLOW_PRIORITY = 32768;


    private static OFFactory factory;

    public static void setSwitch(DatapathId sw) {
        FlowFactory.sw = sw;
    }

    public static void setSwitchService(IOFSwitchService switchService) {
        FlowFactory.switchService = switchService;
    }

    static void insertPrefixRewriteFlows(IPv4AddressWithMask internal, IPv4AddressWithMask external) {

        Runnable task = () -> {
            if (sw.equals(DatapathId.NONE)) {
                log.error("Unable to insert rewrite flows. Switch DPID is not yet configured. Is it connected?");
                return;
            }
            if (switchService == null) {
                log.error("Unable to insert rewrite flows. Switch service is not yet configured.");
                return;
            }

            log.info("Inserting prefix rewrite flow: {} ----> {}", internal, external);
            IOFSwitch iofSwitch = switchService.getActiveSwitch(sw);
            if (iofSwitch != null) {
                factory = iofSwitch.getOFFactory();
                RewriteFlows prf = new PrefixRewriteFlows(internal, external);
                iofSwitch.write(prf.getFlows());
            } else {
                log.error("Switch object not found. Prefix flows will not be inserted");
            }
        };

        executorService.execute(task);
    }

    private interface IRewriteFlows {
        List<OFMessage> getFlows();
    }

    private static abstract class RewriteFlows implements IRewriteFlows {
        @Override
        public List<OFMessage> getFlows() {
            List<OFMessage> flows = new ArrayList<>();
            flows.addAll(getSourceFlows());
            flows.addAll(getDestinationFlows());
            return flows;
        }

        abstract List<OFMessage> getSourceFlows();

        abstract List<OFMessage> getDestinationFlows();

    }

    private static class PrefixRewriteFlows extends RewriteFlows {
        private final RewriteMatches rewriteMatches;
        private final RewriteActions rewriteActions;

        PrefixRewriteFlows(IPv4AddressWithMask internal, IPv4AddressWithMask external) {
            this.rewriteMatches = new RewriteMatches(internal, external);
            this.rewriteActions = new RewriteActions(internal, external);
        }

        @Override
        List<OFMessage> getSourceFlows() {
            List<OFMessage> flows = new ArrayList<>();
            flows.add(getPrefixRewriteFlow(
                    RewriteAttributes.of(EthType.IPv4, RewriteField.SOURCE, RewriteAction.ENCRYPT), SOURCE_REWRITE_TABLE));
            flows.add(getPrefixRewriteFlow(
                    RewriteAttributes.of(EthType.IPv4, RewriteField.SOURCE, RewriteAction.DECRYPT), SOURCE_REWRITE_TABLE));
            flows.add(getPrefixRewriteFlow(
                    RewriteAttributes.of(EthType.ARP, RewriteField.SOURCE, RewriteAction.ENCRYPT), SOURCE_REWRITE_TABLE));
            flows.add(getPrefixRewriteFlow(
                    RewriteAttributes.of(EthType.ARP, RewriteField.SOURCE, RewriteAction.DECRYPT), SOURCE_REWRITE_TABLE));
            return flows;
        }

        @Override
        List<OFMessage> getDestinationFlows() {
            List<OFMessage> flows = new ArrayList<>();
            flows.add(getPrefixRewriteFlow(
                    RewriteAttributes.of(EthType.IPv4, RewriteField.DESTINATION, RewriteAction.ENCRYPT), DESTINATION_REWRITE_TABLE));
            flows.add(getPrefixRewriteFlow(
                    RewriteAttributes.of(EthType.IPv4, RewriteField.DESTINATION, RewriteAction.DECRYPT), DESTINATION_REWRITE_TABLE));
            flows.add(getPrefixRewriteFlow(
                    RewriteAttributes.of(EthType.ARP, RewriteField.DESTINATION, RewriteAction.ENCRYPT), DESTINATION_REWRITE_TABLE));
            flows.add(getPrefixRewriteFlow(
                    RewriteAttributes.of(EthType.ARP, RewriteField.DESTINATION, RewriteAction.DECRYPT), DESTINATION_REWRITE_TABLE));
            return flows;
        }

        OFMessage getPrefixRewriteFlow(RewriteAttributes attr, TableId table) {
            OFFlowAdd.Builder fab = factory.buildFlowAdd()
                    .setBufferId(OFBufferId.NO_BUFFER)
                    .setHardTimeout(HARD_TIMEOUT)
                    .setIdleTimeout(IDLE_TIMEOUT)
                    .setPriority(FLOW_PRIORITY)
                    .setMatch(rewriteMatches.get(attr))
                    //.setActions(rewriteActions.get(attr))
                    .setTableId(table);

            List<OFInstruction> instructions = new ArrayList<>();
            instructions.add(getApplyActionsInstruction(rewriteActions.get(attr)));
            if (!table.equals(FINAL_TABLE)) {
                instructions.add(getTableInstruction(TableId.of(table.getValue() + 1)));

            }
            fab.setInstructions(instructions);
            return fab.build();
        }

        OFInstruction getApplyActionsInstruction(List<OFAction> actions) {
            IPv4AddressWithMask external = IPv4AddressWithMask.of("20.0.0.0/24");
            OFOxms oxms = factory.oxms();
            OFOxm oxm = oxms.buildIpv4SrcMasked().setValue(external.getValue()).setMask(external.getMask()).build();
            //OFOxm oxm = oxms.buildIpv4Src().setValue(external.getValue()).build();
            List<OFAction> actionList = new ArrayList<>();
            actionList.add(factory.actions()
                    .buildSetField()
                    .setField(oxm)
                    .build());
            return factory.instructions().buildApplyActions().setActions(actionList).build();
            //return factory.instructions().buildApplyActions().setActions(actions).build();
        }

        OFInstruction getTableInstruction(TableId tableId) {
            return factory.instructions().buildGotoTable().setTableId(tableId).build();
        }

    }

    private static class RewriteMatches {
        private Map<RewriteAttributes, Match> matches = new HashMap<>();

        RewriteMatches(IPv4AddressWithMask internal, IPv4AddressWithMask external) {
            initMatch(EthType.IPv4, RewriteField.SOURCE, RewriteAction.ENCRYPT, MatchField.IPV4_SRC, internal);
            initMatch(EthType.IPv4, RewriteField.SOURCE, RewriteAction.DECRYPT, MatchField.IPV4_SRC, external);

            initMatch(EthType.IPv4, RewriteField.DESTINATION, RewriteAction.DECRYPT, MatchField.IPV4_DST, external);
            initMatch(EthType.IPv4, RewriteField.DESTINATION, RewriteAction.ENCRYPT, MatchField.IPV4_DST, internal);


            initMatch(EthType.ARP, RewriteField.SOURCE, RewriteAction.ENCRYPT, MatchField.ARP_SPA, internal);
            initMatch(EthType.ARP, RewriteField.SOURCE, RewriteAction.DECRYPT, MatchField.ARP_SPA, external);

            initMatch(EthType.ARP, RewriteField.DESTINATION, RewriteAction.DECRYPT, MatchField.ARP_TPA, external);
            initMatch(EthType.ARP, RewriteField.DESTINATION, RewriteAction.ENCRYPT, MatchField.ARP_TPA, internal);
        }

        private void initMatch(EthType ethType, RewriteField field, RewriteAction action,
                               MatchField<IPv4Address> matchField, IPv4AddressWithMask value) {
            RewriteAttributes attr = RewriteAttributes.of(ethType, field, action);
            matches.put(attr, factory.buildMatch()
                    .setExact(MatchField.ETH_TYPE, attr.ethType)
                    .setExact(matchField, value.getValue())
                    .build());
        }

        Match get(RewriteAttributes attr) {
            return matches.get(attr);
        }
    }

    private static class RewriteActions {
        private Map<RewriteAttributes, List<OFAction>> actions = new HashMap<>();

        RewriteActions(IPv4AddressWithMask internal, IPv4AddressWithMask external) {
            OFOxms oxms = factory.oxms();
//            initAction(EthType.IPv4, RewriteField.SOURCE, RewriteAction.ENCRYPT,
//                    oxms.buildIpv4SrcMasked().setValue(external.getValue()).setMask(external.getMask()).build());
//            initAction(EthType.IPv4, RewriteField.SOURCE, RewriteAction.DECRYPT,
//                    oxms.buildIpv4SrcMasked().setValue(internal.getValue()).setMask(internal.getMask()).build());
//
//            initAction(EthType.IPv4, RewriteField.DESTINATION, RewriteAction.DECRYPT,
//                    oxms.ipv4DstMasked(internal.getValue(), internal.getMask()));
//            initAction(EthType.IPv4, RewriteField.DESTINATION, RewriteAction.ENCRYPT,
//                    oxms.ipv4DstMasked(external.getValue(), external.getMask()));
//
//
//            initAction(EthType.ARP, RewriteField.SOURCE, RewriteAction.ENCRYPT,
//                    oxms.arpSpaMasked(external.getValue(), external.getMask()));
//            initAction(EthType.ARP, RewriteField.SOURCE, RewriteAction.DECRYPT,
//                    oxms.arpSpaMasked(internal.getValue(), internal.getMask()));
//
//            initAction(EthType.ARP, RewriteField.DESTINATION, RewriteAction.DECRYPT,
//                    oxms.arpTpaMasked(internal.getValue(), internal.getMask()));
//            initAction(EthType.ARP, RewriteField.DESTINATION, RewriteAction.ENCRYPT,
//                    oxms.arpTpaMasked(external.getValue(), external.getMask()));
        }

        private void initAction(EthType ethType, RewriteField field, RewriteAction action,
                                OFOxm oxm) {
            RewriteAttributes attr = RewriteAttributes.of(ethType, field, action);
            List<OFAction> actions = new ArrayList<>();
            actions.add(factory.actions()
                    .buildSetField()
                    .setField(oxm)
                    .build());
            if (attr.field == RewriteField.DESTINATION) {
                OFPort port = (attr.action == RewriteAction.ENCRYPT) ? WAN_PORT : LAN_PORT;
                actions.add(factory.actions()
                        .buildOutput()
                        .setMaxLen(0xFFffFFff)
                        .setPort(port)
                        .build());
            }
            this.actions.put(attr, actions);
        }

        List<OFAction> get(RewriteAttributes attr) {
            return actions.get(attr);
        }
    }

    private static class RewriteAttributes {
        final EthType ethType;
        final RewriteField field;
        final RewriteAction action;

        private RewriteAttributes(EthType ethType, RewriteField field, RewriteAction action) {
            this.ethType = ethType;
            this.field = field;
            this.action = action;
        }

        static RewriteAttributes of(EthType ethType, RewriteField field, RewriteAction action) {
            return new RewriteAttributes(ethType, field, action);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RewriteAttributes that = (RewriteAttributes) o;

            if (ethType != null ? !ethType.equals(that.ethType) : that.ethType != null) {
                return false;
            }
            if (field != that.field) {
                return false;
            }
            return action == that.action;
        }

        @Override
        public int hashCode() {
            int result = ethType != null ? ethType.hashCode() : 0;
            result = 31 * result + (field != null ? field.hashCode() : 0);
            result = 31 * result + (action != null ? action.hashCode() : 0);
            return result;
        }
    }

    private enum RewriteField {
        SOURCE, DESTINATION
    }

    private enum RewriteAction {
        ENCRYPT, DECRYPT
    }
}
