package net.floodlightcontroller.tarn;

import com.google.common.eventbus.EventBus;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
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
import org.projectfloodlight.openflow.protocol.action.OFAction;
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
    public Collection<Session> getSessions() {
        return sessions;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        restApiService = context.getServiceImpl(IRestApiService.class);
        deviceService = context.getServiceImpl(IDeviceService.class);
        
        /* Create event listeners */
        EventListener eventListener = new EventListener(this);

        /* Register event listeners */
        eventBus.register(eventListener);

        flowFactory = new FlowFactoryImpl();
        mappingHandler = new PrefixMappingHandler();
        sessions = new ArrayList<>();
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
        log.debug("OFMessage received.");
        if (msg.getType() == OFType.PACKET_IN) {
            OFPacketIn pi = (OFPacketIn) msg;
            log.debug("Packet in received {}", pi);
            OFPort srcPort = OFMessageUtils.getInPort(pi);
            Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

            if (eth.getEtherType() == EthType.IPv4) {
                log.debug("IPv4 message received.");
                IPv4 ipv4 = (IPv4) eth.getPayload();
                if (ipv4.getProtocol() == IpProtocol.TCP) {
                    log.debug("TCP message received.");
                    TCP tcp = (TCP) ipv4.getPayload();
                    /* If source or destination IP addresses belong to a TARN device, then create a new session */
                    if (mappingHandler.isTarnDevice(ipv4)) {
                        log.info("New TARN TCP session identified.");
                        Session session = buildTCPSession(sw, srcPort, eth, ipv4, tcp);
                        sessions.add(session);
                        List<OFMessage> flows = flowFactory.buildFlows(session);
                        sw.write(flows);
                        sw.write(buildPacketOut(sw, pi));
                        return Command.STOP;
                    }
                } else if (ipv4.getProtocol() == IpProtocol.ICMP) {
                    log.debug("ICMP message received.");
                    /* If source or destination IP addresses belong to a TARN device, then create a new session */
                    if (mappingHandler.isTarnDevice(ipv4)) {
                        log.info("New TARN ICMP session identified.");
                        Session session = buildSession(sw, srcPort, eth, ipv4);
                        sessions.add(session);
                        List<OFMessage> flows = flowFactory.buildFlows(session);
                        sw.write(flows);
                        sw.write(buildPacketOut(sw, pi));
                        return Command.STOP;
                    }
                }
            }

        }

        return Command.CONTINUE;
    }

    /**
     * Builds a new TARN session object based on the various payloads of a packet in message.
     *
     * @param sw     the switch that the message was received on.
     * @param inPort the port that the message was received on
     * @param eth    the ethernet payload of the packet in message
     * @param ipv4   the ipv4 payload of the packet in message
     * @param tcp    the tcp payload of the packet in message
     * @return a new session object
     */
    private Session buildTCPSession(IOFSwitch sw, OFPort inPort, Ethernet eth, IPv4 ipv4, TCP tcp) {
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

    private Session buildSession(IOFSwitch sw, OFPort inPort, Ethernet eth, IPv4 ipv4) {
        Session.Builder session = Session.builder();
        ConnectionAttributes.Builder connection1 = ConnectionAttributes.builder();
        ConnectionAttributes.Builder connection2 = ConnectionAttributes.builder();

        /* Start filling in information about the two connections from the packet in message.
        *  Connection1 will always be the connection that the packet in message belongs to and
        *  Connection2 will always be the opposite connection. It can't yet be known which connection
        *  is inbound and which is outbound. */
        connection1.inPort(inPort)
                .srcIp(ipv4.getSourceAddress())
                .dstIp(ipv4.getDestinationAddress());

        connection2.outPort(inPort);

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
