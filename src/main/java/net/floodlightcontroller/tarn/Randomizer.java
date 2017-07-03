package net.floodlightcontroller.tarn;

import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by geddingsbarrineau on 7/14/16.
 * <p>
 * This is the Randomizer Floodlight module.
 */
public class Randomizer implements IOFSwitchListener, IRandomizerService {

    protected IOFSwitchService switchService;
    private static Logger log = LoggerFactory.getLogger(Randomizer.class);

    private static OFPort lanport;
    private static OFPort wanport;

    private Map<Integer, ASNetwork> autonomousSystems = new HashMap<>();

    protected Randomizer(IOFSwitchService switchService) {
        this.switchService = switchService;
    }

    @Override
    public void addASNetwork(ASNetwork asNetwork) {
        autonomousSystems.put(asNetwork.getASNumber(), asNetwork);
    }

    @Override
    public void addASNetwork(int ASNumber, IPv4AddressWithMask internalPrefix) {

    }

    @Override
    public void removeASNetwork(int ASNumber) {
        autonomousSystems.remove(ASNumber);
    }

    @Override
    public OFPort getLanPort() {
        return lanport;
    }

    @Override
    public void setLanPort(int portnumber) {
        lanport = OFPort.of(portnumber);
    }

    @Override
    public OFPort getWanPort() {
        return wanport;
    }

    @Override
    public void setWanPort(int portnumber) {
        wanport = OFPort.of(portnumber);
    }


    //================================================================================
    //region Helper Functions

    //    private void scheduleJobs() {
    //        SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
    //        Scheduler scheduler = null;
    //        try {
    //            scheduler = schedulerFactory.getScheduler();
    //        } catch (SchedulerException e) {
    //            e.printStackTrace();
    //        }
    //
    //        Trigger prefixtrigger = newTrigger()
    //                .withIdentity("prefixtrigger")
    //                .startAt(evenMinuteDateAfterNow()) // get the next even-minute (seconds zero ("**:00"))
    //                .withSchedule(simpleSchedule()
    //                        .withIntervalInSeconds(prefixUpdateInterval)
    //                        .repeatForever())
    //                .build();
    //
    //        JobDetail prefixjob = JobBuilder.newJob(PrefixUpdateJob.class)
    //                .withIdentity("Prefix Update")
    //                .build();
    //
    //        Trigger addresstrigger = newTrigger()
    //                .withIdentity("addresstrigger")
    //                .startAt(evenMinuteDateAfterNow()) // get the next even-minute (seconds zero ("**:00"))
    //                .withSchedule(simpleSchedule()
    //                        .withIntervalInSeconds(addressUpdateInterval)
    //                        .repeatForever())
    //                .build();
    //
    //        JobDetail addressjob = JobBuilder.newJob(AddressUpdateJob.class)
    //                .withIdentity("Address Update")
    //                .build();
    //
    //        try {
    //            if (scheduler != null) {
    //                scheduler.scheduleJob(prefixjob, prefixtrigger);
    //                scheduler.scheduleJob(addressjob, addresstrigger);
    //                scheduler.start();
    //            }
    //        } catch (SchedulerException e) {
    //            e.printStackTrace();
    //        }
    //    }
    //
    //    public static class AddressUpdateJob implements Job {
    //        Logger log = LoggerFactory.getLogger(AddressUpdateJob.class);
    //
    //        @Override
    //        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    //            log.debug("Updating IP addresses for each server. Flows will be updated as well.");
    //            hostList.updateHosts();
    //            connections.forEach(Connection::update);
    //        }
    //    }
    //
    //    public static class PrefixUpdateJob implements Job {
    //        Logger log = LoggerFactory.getLogger(PrefixUpdateJob.class);
    //
    //        @Override
    //        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    //            log.debug("Updating prefixes for each server.");
    //            hostList.getHosts().forEach(RandomizedHost::updatePrefix);
    //        }
    //    }

    //endregion
    //================================================================================


    //================================================================================
    //region IOFSwitchListener Implementation
    @Override
    public void switchAdded(DatapathId switchId) {
        FlowFactory.setSwitch(switchId);
        FlowFactory.setSwitchService(switchService);
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
