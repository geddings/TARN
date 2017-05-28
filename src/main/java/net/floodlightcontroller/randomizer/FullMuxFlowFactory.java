package net.floodlightcontroller.randomizer;

import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;

import java.util.List;

/**
 * Created by geddingsbarrineau on 4/15/17.
 */
public class FullMuxFlowFactory extends AbstractFlowFactory {

    private FullMuxConnection connection;

    public FullMuxFlowFactory(FullMuxConnection connection) {
        this.connection = connection;
    }

    @Override
    protected Match getMatch(EthType ethType) {
        if (ethType == EthType.IPv4) {
            return getIPv4Match();
        } else if (ethType == EthType.ARP) {
            return getARPMatch();
        }
        return null;
    }

    private Match getIPv4Match() {
        Match.Builder mb = factory.buildMatch();
        mb = mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
        mb = mb.setExact(MatchField.IPV4_SRC, connection.getSource().getAddressForMatch(connection.getDirection()));
        mb = mb.setExact(MatchField.IPV4_DST, connection.getDestination().getAddressForMatch(connection.getDirection()));
        return mb.build();
    }

    private Match getARPMatch() {
        Match.Builder mb = factory.buildMatch();
        mb = mb.setExact(MatchField.ETH_TYPE, EthType.ARP);
        mb = mb.setExact(MatchField.ARP_SPA, connection.getSource().getAddressForMatch(connection.getDirection()));
        mb = mb.setExact(MatchField.ARP_TPA, connection.getDestination().getAddressForMatch(connection.getDirection()));
        return mb.build();
    }

    @Override
    protected List<OFAction> getActions(EthType ethType) {
        return null;
    }
}
