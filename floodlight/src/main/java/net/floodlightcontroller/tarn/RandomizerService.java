package net.floodlightcontroller.tarn;

import com.google.common.eventbus.EventBus;
import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.tarn.web.RandomizerWebRoutable;
import net.floodlightcontroller.util.OFMessageUtils;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by geddingsbarrineau on 6/12/17.
 */
public class RandomizerService implements IFloodlightModule, IRandomizerService, IOFSwitchListener, IOFMessageListener {
    private static final Logger log = LoggerFactory.getLogger(RandomizerService.class);

    private OFPort lanport;
    private OFPort wanport;

    private IRestApiService restApiService;
    private IOFSwitchService switchService;

    static final EventBus eventBus = new EventBus();

    private List<AutonomousSystem> autonomousSystems;
    private List<Host> hosts;

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
        FlowFactory.setLanPort(portnumber);
    }

    @Override
    public OFPort getWanPort() {
        return wanport;
    }

    @Override
    public void setWanPort(int portnumber) {
        FlowFactory.setWanPort(portnumber);
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService = context.getServiceImpl(IRestApiService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);

        /* Add service as switch listener */
        switchService.addOFSwitchListener(this);
        
        /* Create event listeners */
        EventListener eventListener = new EventListener(this);

        /* Register event listeners */
        eventBus.register(eventListener);

        autonomousSystems = new ArrayList<>();
        hosts = new ArrayList<>();
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService.addRestletRoutable(new RandomizerWebRoutable());

        parseConfigOptions(context.getConfigParams(this));

        FlowFactory.setSwitchService(switchService);

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
        return l;
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {

        if (msg.getType() == OFType.PACKET_IN) {
            OFPacketIn pi = (OFPacketIn) msg;
            Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
            if (eth.getEtherType() == EthType.ARP) {
                log.info("ARP packet received in randomizer service!");
                handleArp(sw, pi, eth);
                return Command.STOP;
            }
        }
        return Command.CONTINUE;
    }

    private void handleArp(IOFSwitch sw, OFPacketIn pi, Ethernet eth) {
        OFPort inPort = OFMessageUtils.getInPort(pi);
        OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();
        List<OFAction> actions = new ArrayList<>();
        OFPort outPort = inPort.equals(lanport) ? wanport : lanport;
        actions.add(sw.getOFFactory().actions().output(outPort, Integer.MAX_VALUE));
        pob.setActions(actions);
        pob.setBufferId(OFBufferId.NO_BUFFER);
        OFMessageUtils.setInPort(pob, inPort);
        ARP arp = (ARP) eth.getPayload();
        pob.setData(pi.getData());
        sw.write(pob.build());
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
        FlowFactory.setSwitch(switchId);
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
