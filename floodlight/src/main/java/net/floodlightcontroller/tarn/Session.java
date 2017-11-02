package net.floodlightcontroller.tarn;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 11/2/17.
 */
public class Session {
    private final ConnectionAttributes inbound;
    private final ConnectionAttributes outbound;

    private Session(ConnectionAttributes inbound, ConnectionAttributes outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    public ConnectionAttributes getInbound() {
        return inbound;
    }

    public ConnectionAttributes getOutbound() {
        return outbound;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ConnectionAttributes inbound = null;
        private ConnectionAttributes outbound = null;

        public Builder inbound(ConnectionAttributes inbound) {
            this.inbound = inbound;
            return this;
        }

        public Builder outbound(ConnectionAttributes outbound) {
            this.outbound = outbound;
            return this;
        }

        public Session build() {
            if (inbound != null && outbound != null) {
                return new Session(inbound, outbound);
            } else {
                throw new IllegalArgumentException("Session inbound and outbound attributes must be defined.");
            }
        }
    }
}
