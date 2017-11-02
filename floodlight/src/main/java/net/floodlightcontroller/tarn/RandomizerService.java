package net.floodlightcontroller.tarn;

import com.google.common.eventbus.EventBus;
import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.packet.*;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.tarn.web.RandomizerWebRoutable;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by geddingsbarrineau on 6/12/17.
 */
public class RandomizerService implements IFloodlightModule, TarnService, IRandomizerService, IOFSwitchListener, IOFMessageListener {
    private static final Logger log = LoggerFactory.getLogger(RandomizerService.class);

    private OFPort lanport;
    private OFPort wanport;

    private DatapathId rewriteSwitch = DatapathId.NONE;

    private IRestApiService restApiService;
    private IOFSwitchService switchService;
    private IDeviceService deviceService;

    static final EventBus eventBus = new EventBus();

    private List<AutonomousSystem> autonomousSystems;
    private List<Host> hosts;

    private FlowFactory flowFactory;
    
    private PrefixMappingHandler mappingHandler;

    private List<Session> sessions;

    @Override
    public void addAutonomousSystem(AutonomousSystem as) {
        autonomousSystems.add(as);
    }

    @Override
    public void addAutonomousSystem(int asnumber, String internalPrefix) {
        autonomousSystems.add(new AutonomousSystem(asnumber, internalPrefix));
    }

    @Override
    public void removeAutonomousSystem(int asnumber) {
        List<AutonomousSystem> toRemove = autonomousSystems.stream()
                .filter(as -> as.getASNumber() == asnumber)
                .collect(Collectors.toList());
        autonomousSystems.removeAll(toRemove);
    }

    @Override
    public List<AutonomousSystem> getAutonomousSystems() {
        return autonomousSystems;
    }

    @Override
    public Optional<AutonomousSystem> getAutonomousSystem(int asNumber) {
        return autonomousSystems.stream()
                .filter(as -> as.getASNumber() == asNumber)
                .findAny();
    }

    @Override
    public void addHost(Host host) {
        hosts.add(host);
    }

    @Override
    public void removeHost(Host host) {
        hosts.remove(host);
    }

    @Override
    public List<Host> getHosts() {
        return hosts;
    }

    @Override
    public OFPort getLanPort() {
        return lanport;
    }

    @Override
    public void setLanPort(int portnumber) {
        FlowFactoryImpl.setLanPort(portnumber);
    }

    @Override
    public OFPort getWanPort() {
        return wanport;
    }

    @Override
    public void setWanPort(int portnumber) {
        FlowFactoryImpl.setWanPort(portnumber);
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService = context.getServiceImpl(IRestApiService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        deviceService = context.getServiceImpl(IDeviceService.class);

        /* Add service as switch listener */
        switchService.addOFSwitchListener(this);
        
        /* Create event listeners */
        EventListener eventListener = new EventListener(this);

        /* Register event listeners */
        eventBus.register(eventListener);

        autonomousSystems = new ArrayList<>();
        hosts = new ArrayList<>();

        flowFactory = new FlowFactoryImpl();
        
        mappingHandler = new PrefixMappingHandler();
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new RandomizerWebRoutable());

        parseConfigOptions(context.getConfigParams(this));

        FlowFactoryImpl.setSwitchService(switchService);

    }

    private void parseConfigOptions(Map<String, String> configOptions) {
        try {
            /* These are defaults */
            lanport = OFPort.of(Integer.parseInt(configOptions.get("lanport")));
            wanport = OFPort.of(Integer.parseInt(configOptions.get("wanport")));
        } catch (IllegalArgumentException | NullPointerException ex) {
            log.error("Incorrect Randomizer configuration options. Required: 'lanport', " +
                    "'wanport'", ex);
            throw ex;
        }
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> s = new HashSet<>();
        s.add(IRandomizerService.class);
        return s;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<>();
        m.put(IRandomizerService.class, this);
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<>();
        l.add(IOFSwitchService.class);
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
        if (msg.getType() == OFType.PACKET_IN) {
            OFPacketIn pi = (OFPacketIn) msg;
            OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));
            Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

            if (eth.getEtherType() == EthType.IPv4) {
                IPv4 ipv4 = (IPv4) eth.getPayload();
                if (ipv4.getProtocol() == IpProtocol.TCP) {
                    TCP tcp = (TCP) ipv4.getPayload();
                    /* If source or destination IP addresses belong to a TARN device, then get the out port using the mac and create a new session */
                    if (mappingHandler.isTarnDevice(ipv4.getSourceAddress()) || mappingHandler.isTarnDevice(ipv4.getDestinationAddress())) {
                        /* Get the device associated with the destination mac address */
                        Iterator<? extends IDevice> iter = deviceService.queryDevices(eth.getDestinationMACAddress(), VlanVid.ZERO, IPv4Address.NONE,
                                IPv6Address.NONE, DatapathId.NONE, OFPort.ZERO);
                        if (iter.hasNext()) {
                            IDevice nextHop = iter.next();
                            /* Get the output port */
                            for (SwitchPort switchPort : nextHop.getAttachmentPoints()) {
                                if (switchPort.getNodeId().equals(sw.getId())) {
                                    OFPort outPort = switchPort.getPortId();

                                    // FIXME: This is just a placeholder. We don't really know if it's inbound or outbound yet
                                    ConnectionAttributes inbound = ConnectionAttributes.builder()
                                            .build();

                                    ConnectionAttributes outbound = ConnectionAttributes.builder()
                                            .build();

                                    Session session = Session.builder()
                                            .inbound(inbound)
                                            .outbound(outbound)
                                            .build();

                                    sessions.add(session);
                                }
                            }
                        }
                    }

                    return Command.STOP;
                }
            }

        }

        return Command.CONTINUE;
    }

    boolean isTarnPacket(IPv4 ipv4) {
        return false;
    }

    boolean isTarnPacket(IPv6 ipv6) {
        return false;
    }

    public void sendGratuitiousArp(Host host) {
        IPv4Address ip = host.getExternalAddress();
        Iterator<? extends IDevice> iterator = deviceService.queryDevices(MacAddress.NONE, VlanVid.ZERO, host.getInternalAddress(), IPv6Address.NONE, DatapathId.NONE, OFPort.ZERO);
        if (iterator.hasNext()) {
            MacAddress mac = iterator.next().getMACAddress();
            if (rewriteSwitch != DatapathId.NONE) {
                IOFSwitch sw = switchService.getActiveSwitch(rewriteSwitch);
                if (sw != null) {
                    ARP arp = new ARP();
                    arp.setOpCode(ArpOpcode.REQUEST);
                    arp.setSenderHardwareAddress(mac);
                    arp.setSenderProtocolAddress(ip);
                    arp.setTargetHardwareAddress(MacAddress.BROADCAST);
                    arp.setTargetProtocolAddress(ip);

                    OFPacketOut po = sw.getOFFactory().buildPacketOut()
                            .setData(arp.serialize())
                            .setActions(Collections.singletonList(sw.getOFFactory().actions().output(OFPort.FLOOD, 0xffFFffFF)))
                            .setInPort(OFPort.CONTROLLER)
                            .build();

                    sw.write(po);
                }
            }
        } else {
            log.warn("Host {} is not yet known by the device manager. Cannot sent gratuitious ARP.", ip);
        }
    }

    @Override
    public String getName() {
        return IRandomizerService.class.getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));
    }

    @Override
    public void switchAdded(DatapathId switchId) {
        FlowFactoryImpl.setSwitch(switchId);
        rewriteSwitch = switchId;
    }

    @Override
    public void switchRemoved(DatapathId switchId) {

    }

    @Override
    public void switchActivated(DatapathId switchId) {

    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {

    }

    @Override
    public void switchChanged(DatapathId switchId) {

    }

    @Override
    public void switchDeactivated(DatapathId switchId) {

    }
}
