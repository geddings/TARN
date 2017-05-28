package net.floodlightcontroller.randomizer;

import ch.qos.logback.classic.Level;
import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.forwarding.Forwarding;
import net.floodlightcontroller.linkdiscovery.internal.LinkDiscoveryManager;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.randomizer.web.RandomizerWebRoutable;
import net.floodlightcontroller.restserver.IRestApiService;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static org.quartz.DateBuilder.evenMinuteDateAfterNow;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by geddingsbarrineau on 7/14/16.
 * <p>
 * This is the Randomizer Floodlight module.
 */
public class Randomizer implements IOFMessageListener, IOFSwitchListener, IFloodlightModule, IRandomizerService {

    //================================================================================
    //region Properties
    private ScheduledExecutorService executorService;
    private IFloodlightProviderService floodlightProvider;
    private IRestApiService restApiService;
    protected static IOFSwitchService switchService;
    private static Logger log;

    private static List<Connection> connections;
    private static HostManager hostManager;

    private static boolean enabled;
    private static boolean randomize;
    private static OFPort lanport;
    private static OFPort wanport;
    private static int addressUpdateInterval;
    private static int prefixUpdateInterval;

    //endregion
    //================================================================================

    //================================================================================
    //region Helper Functions

    private void scheduleJobs() {
        SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
        Scheduler scheduler = null;
        try {
            scheduler = schedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        Trigger prefixtrigger = newTrigger()
                .withIdentity("prefixtrigger")
                .startAt(evenMinuteDateAfterNow()) // get the next even-minute (seconds zero ("**:00"))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(prefixUpdateInterval)
                        .repeatForever())
                .build();

        JobDetail prefixjob = JobBuilder.newJob(PrefixUpdateJob.class)
                .withIdentity("Prefix Update")
                .build();

        Trigger addresstrigger = newTrigger()
                .withIdentity("addresstrigger")
                .startAt(evenMinuteDateAfterNow()) // get the next even-minute (seconds zero ("**:00"))
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(addressUpdateInterval)
                        .repeatForever())
                .build();

        JobDetail addressjob = JobBuilder.newJob(AddressUpdateJob.class)
                .withIdentity("Address Update")
                .build();

        try {
            if (scheduler != null) {
                scheduler.scheduleJob(prefixjob, prefixtrigger);
                scheduler.scheduleJob(addressjob, addresstrigger);
                scheduler.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static class AddressUpdateJob implements Job {
        Logger log = LoggerFactory.getLogger(AddressUpdateJob.class);

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            log.debug("Updating IP addresses for each server. Flows will be updated as well.");
            hostManager.updateHosts();
            connections.forEach(Connection::update);
        }
    }

    public static class PrefixUpdateJob implements Job {
        Logger log = LoggerFactory.getLogger(PrefixUpdateJob.class);

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            log.debug("Updating prefixes for each server.");
            hostManager.getHosts().forEach(RandomizedHost::updatePrefix);
        }
    }

    //endregion
    //================================================================================

    //================================================================================
    //region IRandomizerService Implementation

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public RandomizerReturnCode enable() {
        log.warn("Enabling Randomizer");
        enabled = true;
        return RandomizerReturnCode.ENABLED;
    }

    @Override
    public RandomizerReturnCode disable() {
        log.warn("Disabling Randomizer");
        enabled = false;
        return RandomizerReturnCode.DISABLED;
    }

    @Override
    public boolean isRandom() {
        return randomize;
    }

    @Override
    public RandomizerReturnCode setRandom(Boolean random) {
        randomize = random;
        log.warn("Set randomize to {}", random);
        return RandomizerReturnCode.CONFIG_SET;
    }

    @Override
    public OFPort getLanPort() {
        return lanport;
    }

    @Override
    public RandomizerReturnCode setLanPort(int portnumber) {
        lanport = OFPort.of(portnumber);
        FlowFactory.setLanport(lanport);
        log.warn("Set lanport to {}", portnumber);
        return RandomizerReturnCode.CONFIG_SET;
    }

    @Override
    public OFPort getWanPort() {
        return wanport;
    }

    @Override
    public RandomizerReturnCode setWanPort(int portnumber) {
        wanport = OFPort.of(portnumber);
        FlowFactory.setWanport(wanport);
        log.warn("Set wanport to {}", portnumber);
        return RandomizerReturnCode.CONFIG_SET;
    }

    @Override
    public RandomizedHost getServer(IPv4Address serveraddress) {
        return hostManager.getServerFromAddress(serveraddress);
    }

    @Override
    public List<RandomizedHost> getServers() {
        return hostManager.getHosts();
    }

    @Override
    public RandomizerReturnCode addServer(RandomizedHost randomizedHost) {
        // Todo Make this portion more robust by adding more checks as needed
        hostManager.addHost(randomizedHost);
        return RandomizerReturnCode.SERVER_ADDED;
    }

    @Override
    public RandomizerReturnCode removeServer(RandomizedHost randomizedHost) {
        // Todo Make this portion more robust by adding more checks as needed
        hostManager.removeHost(randomizedHost);
        return RandomizerReturnCode.SERVER_REMOVED;
    }

    @Override
    public List<Connection> getConnections() {
        return connections;
    }

    @Override
    public RandomizerReturnCode addConnection(Connection connection) {
        connections.add(connection);
        return RandomizerReturnCode.CONNECTION_ADDED;
    }

    @Override
    public RandomizerReturnCode removeConnection(Connection connection) {
        connections.remove(connection);
        return RandomizerReturnCode.CONNECTION_REMOVED;
    }

    @Override
    public Map<IPv4Address, IPv4AddressWithMask> getCurrentPrefix() {
//        return hostManager.getHosts().stream()
//                .collect(Collectors.toMap(RandomizedHost::getAddress, RandomizedHost::getPrefix));
        throw new UnsupportedOperationException();
    }

    public Map<IPv4Address, List<IPv4AddressWithMask>> getPrefixes() {
//        return hostManager.getHosts().stream()
//                .collect(Collectors.toMap(RandomizedHost::getAddress, RandomizedHost::getPrefixes));
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPrefix(RandomizedHost randomizedHost, IPv4AddressWithMask prefix) {
//        if (!hostManager.getServerFromAddress(randomizedHost.getAddress()).getPrefixes().contains(prefix)) {
//            // TODO: This can be simplified a ton.
//            hostManager.getServerFromAddress(randomizedHost.getAddress()).addPrefix(prefix);
//        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePrefix(RandomizedHost randomizedHost, IPv4AddressWithMask prefix) {
//        if (hostManager.getServerFromAddress(randomizedHost.getAddress()).getPrefixes().contains(prefix)) {
//            // TODO: This can also be simplified a lot.
//            hostManager.getServerFromAddress(randomizedHost.getAddress()).removePrefix(prefix);
//        }
        throw new UnsupportedOperationException();
    }

    //endregion
    //================================================================================

    //================================================================================
    //region IOFMessageListener Implementation
    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        /*
         * If we're disabled, then just stop now
		 * and let Forwarding/Hub handle the connection.
		 */
        if (!enabled) {
            log.trace("Randomizer disabled. Not acting on packet; passing to next module.");
            return Command.CONTINUE;
        } else {
            log.trace("Randomizer enabled. Inspecting packet to see if it's a candidate for randomization.");
        }
        
        OFPacketIn pi = (OFPacketIn) msg;
        OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));
        Ethernet l2 = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

        if (packetBelongsToExistingConnection(l2)) {
            log.error("Packet belongs to an existing connection: {}", l2);
            return Command.STOP;
        }
        
        Connection connection = createConnectionFromPacket(l2, sw, inPort);
        
        if (connection != null) {
            log.info("New connection added: {}", connection);
            connections.add(connection);
            return Command.STOP;
        }
        
        return Command.CONTINUE;
    }

    private boolean packetBelongsToExistingConnection(Ethernet l2) {
        EthType ethType = l2.getEtherType();
        Connection connection = createConnectionFromPacket(l2, null, null);

        for (Connection c : connections) {
            if (c.equals(connection)) return true;
        }

        return false;
    }

    private Connection createConnectionFromPacket(Ethernet l2, IOFSwitch sw, OFPort inPort) {
        EthType ethType = l2.getEtherType();
        IHost source = null;
        IHost destination = null;
        Direction direction = null;

        if (inPort != null) direction = inPort.equals(wanport)
                ? Direction.INCOMING
                : Direction.OUTGOING;

        if (ethType == EthType.IPv4) {
            IPv4 l3 = (IPv4) l2.getPayload();
            source = hostManager.getServer(l3.getSourceAddress());
            destination = hostManager.getServer(l3.getDestinationAddress());
        } else if (l2.getEtherType() == EthType.ARP) {
            ARP arp = (ARP) l2.getPayload();
            source = hostManager.getServer(arp.getSenderProtocolAddress());
            destination = hostManager.getServer(arp.getTargetProtocolAddress());
        }

        if (source == null || destination == null) {
            return null;
        } else {
            return new Connection(source, destination, direction, sw.getId(), switchService);
        }
    }


    @Override
    public String getName() {
        return Randomizer.class.getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        if (type.equals(OFType.PACKET_IN) && (name.equals("forwarding"))) {
            log.trace("Randomizer is telling Forwarding to run later.");
            return true;
        } else {
            return false;
        }
    }
    //endregion
    //================================================================================


    //================================================================================
    //region IFloodlightModule Implementation
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
        l.add(IDeviceService.class);
        l.add(IFloodlightProviderService.class);
        l.add(IOFSwitchService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        executorService = Executors.newScheduledThreadPool(2);
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        restApiService = context.getServiceImpl(IRestApiService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        log = LoggerFactory.getLogger(Randomizer.class);

        /* For testing only: Set log levels of other classes */
        ((ch.qos.logback.classic.Logger) log).setLevel(Level.DEBUG);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Forwarding.class)).setLevel(Level.ERROR);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LinkDiscoveryManager.class)).setLevel(Level.ERROR);

        connections = new ArrayList<Connection>();
        hostManager = new HostManager();

        /* Add prefixes here */
        //prefixes.add(IPv4AddressWithMask.of("184.164.243.0/24"));

        /* Add servers here */
        hostManager.addHost(new RandomizedHost(IPv4Address.of(10, 0, 0, 1)));
        hostManager.addHost(new RandomizedHost(IPv4Address.of(20, 0, 0, 1)));
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        switchService.addOFSwitchListener(this);
        restApiService.addRestletRoutable(new RandomizerWebRoutable());

        Map<String, String> configOptions = context.getConfigParams(this);
        try {
            /* These are defaults */
            enabled = Boolean.parseBoolean(configOptions.get("enabled"));
            randomize = Boolean.parseBoolean(configOptions.get("randomize"));
            lanport = OFPort.of(Integer.parseInt(configOptions.get("lanport")));
            wanport = OFPort.of(Integer.parseInt(configOptions.get("wanport")));
            addressUpdateInterval = Integer.parseInt(configOptions.get("addressUpdateIntervalInSeconds"));
            prefixUpdateInterval = Integer.parseInt(configOptions.get("prefixUpdateIntervalInSeconds"));
        } catch (IllegalArgumentException | NullPointerException ex) {
            log.error("Incorrect Randomizer configuration options. Required: 'enabled', 'randomize', 'lanport', 'wanport'", ex);
            throw ex;
        }

        if (log.isInfoEnabled()) {
            log.info("Initial config options: enabled:{}, randomize:{}, lanport:{}, wanport:{}",
                    new Object[]{enabled, randomize, lanport, wanport});
        }

        FlowFactory.setWanport(wanport);
        FlowFactory.setLanport(lanport);

        //updatePrefixes();
        //updateIPs();
        scheduleJobs();
    }
    //endregion
    //================================================================================

    //================================================================================
    //region IOFSwitchListener Implementation
    @Override
    public void switchAdded(DatapathId switchId) {

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
    //endregion
    //================================================================================


}
