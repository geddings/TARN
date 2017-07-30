package net.floodlightcontroller.tarn;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.projectfloodlight.openflow.types.IPv4AddressWithMask;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.floodlightcontroller.tarn.web.AutonomousSystemSerializer;

/**
 * Created by geddingsbarrineau on 5/28/17.
 */
@JsonSerialize(using = AutonomousSystemSerializer.class)
public class AutonomousSystem {

    private final int ASNumber;

    private IPv4AddressWithMask internalPrefix;
    private IPv4AddressWithMask externalPrefix;
    private List<IPv4AddressWithMask> prefixPool;
    private PrefixGenerator generator;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public AutonomousSystem(int ASNumber, IPv4AddressWithMask internalPrefix) {
        this.ASNumber = ASNumber;
        this.internalPrefix = internalPrefix;
        prefixPool = new ArrayList<>();
        generator = new PrefixGenerator(ASNumber);

        Runnable task = () -> {
            IPv4AddressWithMask newPrefix = generator.getNextPrefix();
            RandomizerService.eventBus.post(new PrefixChangeEvent(this, externalPrefix, newPrefix));
            externalPrefix = newPrefix;
        };

        executor.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
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
                return null;
            } else {
                return prefixPool.get(Math.abs(next) % (prefixPool.size()));
            }
        }
    }
}
