package net.floodlightcontroller.tarn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.tarn.events.PrefixChangeEvent;
import net.floodlightcontroller.tarn.web.AutonomousSystemSerializer;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by geddingsbarrineau on 5/28/17.
 * 
 */
@Deprecated
@JsonSerialize(using = AutonomousSystemSerializer.class)
public class AutonomousSystem {

    private final int ASNumber;

    private final IPv4AddressWithMask internalPrefix;
    private IPv4AddressWithMask externalPrefix;
    private final List<IPv4AddressWithMask> prefixPool;
    private final PrefixGenerator generator;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @JsonCreator
    AutonomousSystem(
            @JsonProperty("as-number") int ASNumber,
            @JsonProperty("internal-prefix") String internalPrefix) {
        this.ASNumber = ASNumber;
        this.internalPrefix = IPv4AddressWithMask.of(internalPrefix);
        this.externalPrefix = IPv4AddressWithMask.NONE;
        this.prefixPool = new ArrayList<>();
        this.generator = new PrefixGenerator(ASNumber);

        Runnable task = () -> {
            IPv4AddressWithMask newPrefix = generator.getNextPrefix();
            RandomizerService.eventBus.post(new PrefixChangeEvent(this, externalPrefix, newPrefix));
            externalPrefix = newPrefix;
        };

        executor.scheduleAtFixedRate(task, 0, 15, TimeUnit.SECONDS);
    }

    public int getASNumber() {
        return ASNumber;
    }

    public IPv4AddressWithMask getInternalPrefix() {
        return internalPrefix;
    }

    public IPv4AddressWithMask getExternalPrefix() {
        return externalPrefix;
    }

    public List<IPv4AddressWithMask> getPrefixPool() {
        return prefixPool;
    }

    public void addPrefix(IPv4AddressWithMask prefix) {
        if (!prefixPool.contains(prefix)) {
            prefixPool.add(prefix);
        }
    }

    public void removePrefix(IPv4AddressWithMask prefix) {
        prefixPool.remove(prefix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AutonomousSystem as = (AutonomousSystem) o;

        return ASNumber == as.ASNumber;
    }

    @Override
    public int hashCode() {
        return ASNumber;
    }

    class PrefixGenerator {
        Random rng;

        PrefixGenerator(long seed) {
            int rate = 15;
            rng = new Random(seed);
            long totalUpdates = Instant.now().getEpochSecond() / rate;
            for (int i = 0; i < totalUpdates; i++) rng.nextInt();
        }

        IPv4AddressWithMask getNextPrefix() {
            int next = rng.nextInt();
            if (prefixPool.isEmpty()) {
                return IPv4AddressWithMask.NONE;
            } else {
                return prefixPool.get(Math.abs(next) % (prefixPool.size()));
            }
        }
    }
}
