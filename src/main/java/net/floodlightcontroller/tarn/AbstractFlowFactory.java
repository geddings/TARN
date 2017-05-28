package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geddingsbarrineau on 4/15/17.
 */
public abstract class AbstractFlowFactory implements IFlowFactory {

    protected static OFPort wanport = OFPort.of(1);
    protected static OFPort lanport = OFPort.of(2);
    protected OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);
    private static int hardtimeout = 30;
    private static int idletimeout = 30;
    private static int flowpriority = 32768;

    public static OFPort getWanport() {
        return wanport;
    }

    public static void setWanport(OFPort wanport) {
        AbstractFlowFactory.wanport = wanport;
    }

    public static OFPort getLanport() {
        return lanport;
    }

    static void setLanport(OFPort lanport) {
        AbstractFlowFactory.lanport = lanport;
    }

    public List<OFFlowMod> getFlowAdds() {
        return getFlows(OFFlowModCommand.ADD);
    }

    public  List<OFFlowMod> getFlows(OFFlowModCommand fmc) {
        List<OFFlowMod> flows = new ArrayList<>();

        flows.add(getFlow(EthType.IPv4, fmc));
        flows.add(getFlow(EthType.ARP, fmc));

        return flows;
    }

    private OFFlowMod getFlow(EthType ethType, OFFlowModCommand flowModCommand) {
        OFFlowMod.Builder fmb;
        switch (flowModCommand) {
            case ADD:
                fmb = factory.buildFlowAdd();
                break;
            case DELETE:
                fmb = factory.buildFlowDelete();
                break;
            case DELETE_STRICT:
                fmb = factory.buildFlowDeleteStrict();
                break;
            case MODIFY:
                fmb = factory.buildFlowModify();
                break;
            case MODIFY_STRICT:
                fmb = factory.buildFlowModifyStrict();
                break;
            default:
                // FIXME: This needs to be handled properly.
                return null;
        }
        return fmb.setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(hardtimeout)
                .setIdleTimeout(idletimeout)
                .setPriority(flowpriority)
                .setMatch(getMatch(ethType))
                .setActions(getActions(ethType))
                .build();
    }

    protected abstract Match getMatch(EthType ethType);

    protected abstract List<OFAction> getActions(EthType ethType);

}
