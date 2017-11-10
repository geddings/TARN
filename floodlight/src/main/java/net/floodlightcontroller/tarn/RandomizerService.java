package net.floodlightcontroller.tarn;

import com.google.common.eventbus.EventBus;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.tarn.web.RandomizerWebRoutable;
import net.floodlightcontroller.util.OFMessageUtils;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by geddingsbarrineau on 6/12/17.
 */
public class RandomizerService implements IFloodlightModule, TarnService, IOFMessageListener {
    private static final Logger log = LoggerFactory.getLogger(RandomizerService.class);

    private DatapathId rewriteSwitch = DatapathId.NONE;

    private IRestApiService restApiService;
    private IOFSwitchService switchService;
    private IDeviceService deviceService;

    static final EventBus eventBus = new EventBus();

    private FlowFactory flowFactory;

    private PrefixMappingHandler mappingHandler;

    private List<Session> sessions;

    @Override
    public Collection<PrefixMapping> getPrefixMappings() {
        return mappingHandler.getMappings();
    }

    @Override
    public void addPrefixMapping(PrefixMapping mapping) {
        mappingHandler.addMapping(mapping);
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService = context.getServiceImpl(IRestApiService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        deviceService = context.getServiceImpl(IDeviceService.class);
        
        /* Create event listeners */
        EventListener eventListener = new EventListener(this);

        /* Register event listeners */
        eventBus.register(eventListener);

        flowFactory = new FlowFactoryImpl();

        mappingHandler = new PrefixMappingHandler();
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new RandomizerWebRoutable());

//        parseConfigOptions(context.getConfigParams(this));

    }

    private void parseConfigOptions(Map<String, String> configOptions) {
        try {
            /* These are defaults */
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
        m.put(TarnService.class, this);
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
            DatapathId srcSw = sw.getId();
            OFPort srcPort = OFMessageUtils.getInPort(pi);
            IDevice srcDevice = IDeviceService.fcStore.get(cntx, IDeviceService.CONTEXT_SRC_DEVICE);
            IDevice dstDevice = IDeviceService.fcStore.get(cntx, IDeviceService.CONTEXT_DST_DEVICE);
            Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

            if (eth.getEtherType() == EthType.IPv4) {
                IPv4 ipv4 = (IPv4) eth.getPayload();
                if (ipv4.getProtocol() == IpProtocol.TCP) {
                    TCP tcp = (TCP) ipv4.getPayload();
                    /* If source or destination IP addresses belong to a TARN device, then create a new session */
                    if (mappingHandler.isTarnDevice(ipv4)) {
                        Session session = buildSession(sw, srcPort, eth, ipv4, tcp);
                        sessions.add(session);
                        return Command.STOP;
                    }
                }
            }

        }

        return Command.CONTINUE;
    }

    /**
     * Builds a new TARN session object based on the various payloads of a packet in message.
     * @param sw the switch that the message was received on.
     * @param inPort the port that the message was received on
     * @param eth the ethernet payload of the packet in message
     * @param ipv4 the ipv4 payload of the packet in message
     * @param tcp the tcp payload of the packet in message
     * @return a new session object
     */
    private Session buildSession(IOFSwitch sw, OFPort inPort, Ethernet eth, IPv4 ipv4, TCP tcp) {
        Session.Builder session = Session.builder();
        ConnectionAttributes.Builder connection1 = ConnectionAttributes.builder();
        ConnectionAttributes.Builder connection2 = ConnectionAttributes.builder();

        /* Start filling in information about the two connections from the packet in message.
        *  Connection1 will always be the connection that the packet in message belongs to and
        *  Connection2 will always be the opposite connection. It can't yet be known which connection
        *  is inbound and which is outbound. */
        connection1.inPort(inPort)
                .srcIp(ipv4.getSourceAddress())
                .dstIp(ipv4.getDestinationAddress())
                .srcPort(tcp.getSourcePort())
                .dstPort(tcp.getDestinationPort());

        connection2.outPort(inPort)
                .srcPort(tcp.getDestinationPort())
                .dstPort(tcp.getSourcePort());

        /* Get the output port and add it to the connections */
        Optional<SwitchPort> switchPort = getAttachmentPoint(eth.getDestinationMACAddress(), sw.getId());
        if (switchPort.isPresent()) {
            connection1.outPort(switchPort.get().getPortId());
            connection2.inPort(switchPort.get().getPortId());
        }

        /* Using the source address of Connection1, determine the destination address of Connection2. */
        Optional<PrefixMapping> sourceMapping = mappingHandler.getAssociatedMapping(ipv4.getSourceAddress());
        if (sourceMapping.isPresent()) {
            if (sourceMapping.get().getInternalIp().equals(ipv4.getSourceAddress())) {
                connection2.dstIp(IPGenerator.getRandomAddressFrom(sourceMapping.get().getCurrentPrefix()));
            } else if (sourceMapping.get().getCurrentPrefix().contains(ipv4.getSourceAddress())) {
                connection2.dstIp(sourceMapping.get().getInternalIp());
            }
        } else {
            connection2.dstIp(ipv4.getSourceAddress());
        }

        /* Using the destination address of Connection1, determine the source address of Connection2. */
        Optional<PrefixMapping> destinationMapping = mappingHandler.getAssociatedMapping(ipv4.getDestinationAddress());
        if (destinationMapping.isPresent()) {
            if (destinationMapping.get().getInternalIp().equals(ipv4.getDestinationAddress())) {
                connection2.srcIp(IPGenerator.getRandomAddressFrom(destinationMapping.get().getCurrentPrefix()));
            } else if (destinationMapping.get().getCurrentPrefix().contains(ipv4.getDestinationAddress())) {
                connection2.srcIp(destinationMapping.get().getInternalIp());
            }
        } else {
            connection2.srcIp(ipv4.getDestinationAddress());
        }

        /* Determine which connection is inbound and which is outbound.
         * An outbound connection is one that will match on INTERNAL IP addresses.
         * An inbound connection is one that will match on EXTERNAL IP addresses. */
        if (mappingHandler.containsInternalIp(ipv4)) {
            session.outbound(connection1.build())
                    .inbound(connection2.build());
        } else {
            session.inbound(connection1.build())
                    .outbound(connection2.build());
        }
        return session.build();
    }

    /**
     * Given the mac address of a device and the dpid of a switch, retrieves the attachment point, if one exists
     * @param macAddress the mac address of the device
     * @param dpid the dpid of the switch
     * @return the optional attachment point of device (mac) on the switch (dpid)
     */
    Optional<SwitchPort> getAttachmentPoint(MacAddress macAddress, DatapathId dpid) {
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

    void sendGratuitiousArp(Host host) {
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
