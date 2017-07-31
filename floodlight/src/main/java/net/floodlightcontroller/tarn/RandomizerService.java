package net.floodlightcontroller.tarn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        /* Configure the flow factory for testing */
//        FlowFactory.setSwitch(DatapathId.of(1));
        FlowFactory.setSwitchService(switchService);

        /* Create and configure a few ASes to test with */
//        AutonomousSystem as1 = new AutonomousSystem(1, "10.0.0.0/24");
//        as1.addPrefix(IPv4AddressWithMask.of("20.0.0.0/24"));
//        as1.addPrefix(IPv4AddressWithMask.of("30.0.0.0/24"));
//        autonomousSystems.add(as1);
//
//        AutonomousSystem as2 = new AutonomousSystem(2, "40.0.0.0/24");
//        as2.addPrefix(IPv4AddressWithMask.of("50.0.0.0/24"));
//        as2.addPrefix(IPv4AddressWithMask.of("60.0.0.0/24"));
//        autonomousSystems.add(as2);
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
