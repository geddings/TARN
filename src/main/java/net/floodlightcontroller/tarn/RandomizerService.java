package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.tarn.web.RandomizerWebRoutable;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import com.google.common.eventbus.EventBus;

/**
 * Created by geddingsbarrineau on 6/12/17.
 */
public class RandomizerService implements IFloodlightModule, IRandomizerService {
    private static final Logger log = LoggerFactory.getLogger(RandomizerService.class);

    private Randomizer randomizer;

    private OFPort lanport;
    private OFPort wanport;

    private IRestApiService restApiService;
    private IOFSwitchService switchService;

    public static final EventBus eventBus = new EventBus();

    @Override
    public void addASNetwork(ASNetwork asNetwork) {

    }

    @Override
    public void addASNetwork(int ASNumber, IPv4AddressWithMask internalPrefix) {

    }

    @Override
    public void removeASNetwork(int ASNumber) {

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

        randomizer = new Randomizer(switchService);

        /* Create event listeners */
        EventListener eventListener = new EventListener();

        /* Register event listeners */
        eventBus.register(eventListener);
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        switchService.addOFSwitchListener(randomizer);
        restApiService.addRestletRoutable(new RandomizerWebRoutable());

        parseConfigOptions(context.getConfigParams(this));

        /* Configure the flow factory for testing */
//        FlowFactory.setSwitch(DatapathId.of(1));
//        FlowFactory.setSwitchService(switchService);

        /* Create and configure a few ASes to test with */
        ASNetwork as1 = new ASNetwork(1, IPv4AddressWithMask.of("10.0.0.0/24"));
        as1.addPrefix(IPv4AddressWithMask.of("20.0.0.0/24"));
        as1.addPrefix(IPv4AddressWithMask.of("30.0.0.0/24"));
        randomizer.addASNetwork(as1);

//        ASNetwork as2 = new ASNetwork(2, IPv4AddressWithMask.of("70.0.0.0/24"));
//        as2.addPrefix(IPv4AddressWithMask.of("80.0.0.0/24"));
//        as2.addPrefix(IPv4AddressWithMask.of("90.0.0.0/24"));
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

}
