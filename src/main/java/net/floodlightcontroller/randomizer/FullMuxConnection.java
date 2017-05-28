package net.floodlightcontroller.randomizer;

import net.floodlightcontroller.core.IOFSwitch;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.types.DatapathId;

/**
 * Created by geddingsbarrineau on 4/15/17.
 */
public class FullMuxConnection implements IConnection<RandomizedHost, RandomizedHost> {

    RandomizedHost source;
    RandomizedHost destination;
    Direction direction;
    DatapathId sw;
    FlowFactory flowFactory;

    public FullMuxConnection(RandomizedHost source, RandomizedHost destination, Direction direction, DatapathId sw) {
        this.source = source;
        this.destination = destination;
        this.direction = direction;
        this.sw = sw;

        flowFactory = new FlowFactory(this);

        IOFSwitch ofSwitch = Randomizer.switchService.getActiveSwitch(sw);

        for (OFFlowMod flow : flowFactory.getFlowAdds()) {
            ofSwitch.write(flow);
        }

    }

    @Override
    public void updateConnection() {
        IOFSwitch ofSwitch = Randomizer.switchService.getActiveSwitch(sw);
        if (ofSwitch != null) {
            for (OFFlowMod flow : flowFactory.getFlowAdds()) {
                ofSwitch.write(flow);
            }
        }
    }

    @Override
    public RandomizedHost getSource() {
        return null;
    }

    @Override
    public RandomizedHost getDestination() {
        return null;
    }
}
