package net.floodlightcontroller.tarn.flows;

import net.floodlightcontroller.tarn.AutonomousSystem;
import net.floodlightcontroller.tarn.Host;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 7/30/17.
 */
public class RewriteFlow {

    private OFFactory factory = OFFactories.getFactory(OFVersion.OF_15);

    private AutonomousSystem as;
    private Host host;
    private RewriteField rewriteField;
    private RewriteAction rewriteAction;
    private EthType ethType;
    private OFPort inPort;
    private OFPort outPort;

    private RewriteFlow(Builder builder) {
        this.as = builder.as;
        this.host = builder.host;
        this.rewriteField = builder.rewriteField;
        this.rewriteAction = builder.rewriteAction;
        this.ethType = builder.ethType;
        this.inPort = builder.inPort;
        this.outPort = builder.outPort;
    }

    public OFMessage getFlow() {
        int priority = 100;
        if (host != null) priority = 200;
        
        return factory.buildFlowAdd()
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(60)
                .setIdleTimeout(30)
                .setPriority(priority)
                .setMatch(getMatch())
                .setInstructions(getInstructions())
                .setTableId(getTableId())
                .build();
    }

    private Match getMatch() {
        MatchField<org.projectfloodlight.openflow.types.IPv4Address> matchField = null;
        if (ethType.equals(EthType.IPv4) && rewriteField.equals(RewriteField.SOURCE)) matchField = MatchField.IPV4_SRC;
        else if (ethType.equals(EthType.IPv4) && rewriteField.equals(RewriteField.DESTINATION))
            matchField = MatchField.IPV4_DST;
        else if (ethType.equals(EthType.ARP) && rewriteField.equals(RewriteField.SOURCE))
            matchField = MatchField.ARP_SPA;
        else if (ethType.equals(EthType.ARP) && rewriteField.equals(RewriteField.DESTINATION))
            matchField = MatchField.ARP_TPA;

        if (host != null) {
            IPv4Address matchValue = null;
            if (rewriteAction.equals(RewriteAction.ENCRYPT)) matchValue = host.getInternalAddress();
            else if (rewriteAction.equals(RewriteAction.DECRYPT))
                matchValue = host.getExternalAddress()
                        .and(as.getExternalPrefix().getMask().not())
                        .or(as.getExternalPrefix().getValue());

            return factory.buildMatch()
                    .setExact(MatchField.IN_PORT, inPort)
                    .setExact(MatchField.ETH_TYPE, ethType)
                    .setExact(matchField, matchValue)
                    .build();
        } else {
            IPv4AddressWithMask matchValue = null;
            if (rewriteAction.equals(RewriteAction.ENCRYPT)) matchValue = as.getInternalPrefix();
            else if (rewriteAction.equals(RewriteAction.DECRYPT)) matchValue = as.getExternalPrefix();

            return factory.buildMatch()
                    .setExact(MatchField.IN_PORT, inPort)
                    .setExact(MatchField.ETH_TYPE, ethType)
                    .setMasked(matchField, matchValue)
                    .build();
        }
    }

    private List<OFInstruction> getInstructions() {
        List<OFInstruction> instructions = new ArrayList<>();
        instructions.add(factory.instructions().buildApplyActions().setActions(getActions()).build());

        if (rewriteField.equals(RewriteField.SOURCE)) {
            instructions.add(factory.instructions().buildGotoTable().setTableId(TableId.of(1)).build());
        }

        return instructions;
    }

    private List<OFAction> getActions() {
        List<OFAction> actions = new ArrayList<>();
        OFOxms oxms = factory.oxms();

        OFOxm oxm = null;
        if (host != null) {
            IPv4Address actionValue = IPv4Address.NONE;
            if (rewriteAction.equals(RewriteAction.ENCRYPT))
                actionValue = host.getExternalAddress()
                        .and(as.getExternalPrefix().getMask().not())
                        .or(as.getExternalPrefix().getValue());
            else if (rewriteAction.equals(RewriteAction.DECRYPT)) actionValue = host.getInternalAddress();

            if (ethType.equals(EthType.IPv4) && rewriteField.equals(RewriteField.SOURCE)) oxm = oxms.ipv4Src(actionValue);
            else if (ethType.equals(EthType.IPv4) && rewriteField.equals(RewriteField.DESTINATION)) oxm = oxms.ipv4Dst(actionValue);
            else if (ethType.equals(EthType.ARP) && rewriteField.equals(RewriteField.SOURCE)) oxm = oxms.arpSpa(actionValue);
            else if (ethType.equals(EthType.ARP) && rewriteField.equals(RewriteField.DESTINATION)) oxm = oxms.arpTpa(actionValue);

        } else {
            IPv4AddressWithMask actionValue = IPv4AddressWithMask.NONE;
            if (rewriteAction.equals(RewriteAction.ENCRYPT)) actionValue = as.getExternalPrefix();
            else if (rewriteAction.equals(RewriteAction.DECRYPT)) actionValue = as.getInternalPrefix();

            if (ethType.equals(EthType.IPv4) && rewriteField.equals(RewriteField.SOURCE))
                oxm = oxms.ipv4SrcMasked(actionValue.getValue(), actionValue.getMask());
            else if (ethType.equals(EthType.IPv4) && rewriteField.equals(RewriteField.DESTINATION))
                oxm = oxms.ipv4DstMasked(actionValue.getValue(), actionValue.getMask());
            else if (ethType.equals(EthType.ARP) && rewriteField.equals(RewriteField.SOURCE))
                oxm = oxms.arpSpaMasked(actionValue.getValue(), actionValue.getMask());
            else if (ethType.equals(EthType.ARP) && rewriteField.equals(RewriteField.DESTINATION))
                oxm = oxms.arpTpaMasked(actionValue.getValue(), actionValue.getMask());
        }

        actions.add(factory.actions()
                .buildSetField()
                .setField(oxm)
                .build());

        if (rewriteField.equals(RewriteField.DESTINATION)) {
            actions.add(factory.actions()
                    .buildOutput()
                    .setMaxLen(0xFFffFFff)
                    .setPort(outPort)
                    .build());
        }

        return actions;
    }

    private TableId getTableId() {
        return rewriteField.equals(RewriteField.SOURCE) ? TableId.of(0) : TableId.of(1);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        AutonomousSystem as;
        Host host;
        RewriteField rewriteField;
        RewriteAction rewriteAction;
        EthType ethType;
        OFPort inPort;
        OFPort outPort;

        public Builder as(AutonomousSystem as) {
            this.as = as;
            return this;
        }

        public Builder host(Host host) {
            this.host = host;
            return this;
        }

        public Builder source() {
            this.rewriteField = RewriteField.SOURCE;
            return this;
        }

        public Builder destination() {
            this.rewriteField = RewriteField.DESTINATION;
            return this;
        }

        public Builder encrypt() {
            this.rewriteAction = RewriteAction.ENCRYPT;
            return this;
        }

        public Builder decrypt() {
            this.rewriteAction = RewriteAction.DECRYPT;
            return this;
        }

        public Builder ethType(EthType ethType) {
            this.ethType = ethType;
            return this;
        }

        public Builder inPort(OFPort inPort) {
            this.inPort = inPort;
            return this;
        }

        public Builder outPort(OFPort outPort) {
            this.outPort = outPort;
            return this;
        }

        public RewriteFlow build() {
            return new RewriteFlow(this);
        }
    }
}
