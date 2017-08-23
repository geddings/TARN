package net.floodlightcontroller.tarn;

import com.google.common.eventbus.EventBus;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.tarn.web.RandomizerWebRoutable;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by geddingsbarrineau on 6/12/17.
 */
public class RandomizerService implements IFloodlightModule, IRandomizerService, IOFSwitchListener {
    private static final Logger log = LoggerFactory.getLogger(RandomizerService.class);

    private OFPort lanport;
    private OFPort wanport;

    private IRestApiService restApiService;
    private IOFSwitchService switchService;

    static final EventBus eventBus = new EventBus();

    private List<AutonomousSystem> autonomousSystems;

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
    public OFPort getLanPort() {
        return lanport;
    }

    @Override
    public void setLanPort(int portnumber) {
        lanport = OFPort.of(portnumber);
        //FlowFactory.setLanport(lanport);
        log.warn("Set lanport to {}", portnumber);
    }

    @Override
    public OFPort getWanPort() {
        return wanport;
    }

    @Override
    public void setWanPort(int portnumber) {
        wanport = OFPort.of(portnumber);
        //FlowFactory.setWanport(wanport);
        log.warn("Set wanport to {}", portnumber);
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        restApiService = context.getServiceImpl(IRestApiService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);

        /* Add service as switch listener */
        switchService.addOFSwitchListener(this);
        
        /* Create event listeners */
        EventListener eventListener = new EventListener();

        /* Register event listeners */
        eventBus.register(eventListener);

        autonomousSystems = new ArrayList<>();
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
            log.error("Incorrect Randomizer configuration options. Required: 'enabled', 'randomize', 'lanport', " +
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
