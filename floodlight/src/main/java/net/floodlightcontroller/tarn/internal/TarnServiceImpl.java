package net.floodlightcontroller.tarn.internal;

import com.google.common.eventbus.EventBus;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.AppCookie;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.packet.*;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.tarn.*;
import net.floodlightcontroller.tarn.web.RandomizerWebRoutable;
import net.floodlightcontroller.util.OFMessageUtils;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by geddingsbarrineau on 6/12/17.
 */
public class TarnServiceImpl implements IFloodlightModule, TarnService, IOFMessageListener {
    private static final Logger log = LoggerFactory.getLogger(TarnServiceImpl.class);

    private IFloodlightProviderService floodlightProvider;
    private IRestApiService restApiService;
    private IDeviceService deviceService;

    static final EventBus eventBus = new EventBus();

    private SessionFactory sessionFactory;
    private FlowFactory flowFactory;

    private PrefixMappingHandler mappingHandler;

    private HashMap<U64, Session> sessions;
    public static final int TARN_SERVICE_APP_ID = 99;

    @Override
    public Collection<PrefixMapping> getPrefixMappings() {
        return mappingHandler.getMappings();
    }

    @Override
    public void addPrefixMapping(PrefixMapping mapping) {
        mappingHandler.addMapping(mapping);
    }

    @Override
    public Collection<Session> getSessions() {
        return sessions.values();
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        restApiService = context.getServiceImpl(IRestApiService.class);
        deviceService = context.getServiceImpl(IDeviceService.class);
        
        /* Create event listeners */
        net.floodlightcontroller.tarn.EventListener eventListener = new net.floodlightcontroller.tarn.EventListener(this);

        /* Register event listeners */
        eventBus.register(eventListener);

        mappingHandler = new PrefixMappingHandler();
        sessionFactory = new SessionFactoryImpl(mappingHandler);
        flowFactory = new FlowFactoryImpl();
        sessions = new HashMap<>();
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        restApiService.addRestletRoutable(new RandomizerWebRoutable());
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s = new HashSet<>();
        s.add(TarnService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<>();
        m.put(TarnService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<>();
        l.add(IDeviceService.class);
        return l;
    }

    /**
     * The receive function is used to respond to PacketIn messages and determine whether or not TARN should act on them.
     * <p>
     * TARN will only respond to TCP connections that involve at least one TARN device.
     *
     * @param sw   the OpenFlow switch that sent this message
     * @param msg  the message
     * @param cntx a Floodlight message context object you can use to pass
     *             information between listeners
     * @return Command - whether to pass the message along or stop it here
     */
    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        switch (msg.getType()) {
            case PACKET_IN:
                return this.processPacketInMessage(sw, (OFPacketIn)msg, cntx);
            case FLOW_REMOVED:
                return this.removeInactiveSession((OFFlowRemoved) msg, cntx);
            case ERROR:
                log.info("received an error {} from switch {}", msg, sw);
                return Command.CONTINUE;
            default:
                log.error("received an unexpected message {} from switch {}", msg, sw);
                return Command.CONTINUE;
        }

    }

    private Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx) {
        OFPort inPort = OFMessageUtils.getInPort(pi);
        Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

        if (eth.getEtherType() == EthType.IPv4) {
            IPv4 ipv4 = (IPv4) eth.getPayload();
            if (mappingHandler.isTarnDevice(ipv4)) {
                OFPort outPort = getOutPort(eth.getDestinationMACAddress(), sw.getId());
                Session session = sessionFactory.getSession(inPort, outPort, ipv4);
                if (session != null) {
//                    sessions.add(session);
                    sessions.put(AppCookie.makeCookie(TARN_SERVICE_APP_ID, session.hashCode()), session);
                    List<OFMessage> flows = flowFactory.buildFlows(session);
                    sw.write(flows);
                    sw.write(buildPacketOut(sw, pi));
                    return Command.STOP;
                }
            }

        }

        return Command.CONTINUE;
    }

    private Command removeInactiveSession(OFFlowRemoved flowRemMessage, FloodlightContext cntx) {
        if (!flowRemMessage.getCookie().equals(U64.of(TARN_SERVICE_APP_ID))) {
            return Command.CONTINUE;
        }
        if (log.isTraceEnabled()) {
            log.trace("Old session {} will be removed", sessions.get(flowRemMessage.getCookie()));
        }

//        Match match = flowRemMessage.getMatch();
        // If a flow remove message send to controller, it means that flow already expired
        // We'll then remove the session that associated with that flow, so all the sessions remaining
        // are the active session.
        sessions.remove(flowRemMessage.getCookie());

        return Command.CONTINUE;
    }

    private OFPacketOut buildPacketOut(IOFSwitch sw, OFPacketIn pi) {

        OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();
        List<OFAction> actions = new ArrayList<>();
        actions.add(sw.getOFFactory().actions().output(OFPort.TABLE, Integer.MAX_VALUE));
        pob.setActions(actions);
        pob.setBufferId(OFBufferId.NO_BUFFER);

        if (pob.getBufferId().equals(OFBufferId.NO_BUFFER)) {
            byte[] packetData = pi.getData();
            pob.setData(packetData);
        }

        OFMessageUtils.setInPort(pob, OFMessageUtils.getInPort(pi));

        return pob.build();
    }

    /**
     * Given the mac address of a device and the dpid of a switch, retrieves the attachment point, if one exists
     *
     * @param macAddress the mac address of the device
     * @param dpid       the dpid of the switch
     * @return the optional attachment point of device (mac) on the switch (dpid)
     */
    public Optional<SwitchPort> getAttachmentPoint(MacAddress macAddress, DatapathId dpid) {
        /* Get the device associated with the destination mac address */
        Iterator<? extends IDevice> iter = deviceService.queryDevices(macAddress, VlanVid.ZERO, IPv4Address.NONE,
                IPv6Address.NONE, DatapathId.NONE, OFPort.ZERO);
        if (iter.hasNext()) {
            IDevice nextHop = iter.next();
            /* Get the output port */
            for (SwitchPort switchPort : nextHop.getAttachmentPoints()) {
                if (switchPort.getNodeId().equals(dpid)) {
                    return Optional.of(switchPort);
                }
            }
        }
        return Optional.empty();
    }

    private OFPort getOutPort(MacAddress macAddress, DatapathId dpid) {
        Optional<SwitchPort> switchPort = getAttachmentPoint(macAddress, dpid);
        if (switchPort.isPresent()) {
            return switchPort.get().getPortId();
        } else {
            return OFPort.ANY;
        }
    }

    @Override
    public String getName() {
        return TarnService.class.getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));
    }
}
