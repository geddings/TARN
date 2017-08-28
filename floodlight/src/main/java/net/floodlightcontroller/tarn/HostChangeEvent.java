package net.floodlightcontroller.tarn;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 8/26/17.
 */
public class HostChangeEvent {

    private final Host host;

    HostChangeEvent(Host host) {
        this.host = host;
    }

    public Host getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "HostChangeEvent{" +
                "host=" + host +
                '}';
    }
}
