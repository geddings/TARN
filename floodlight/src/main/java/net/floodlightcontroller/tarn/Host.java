package net.floodlightcontroller.tarn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.floodlightcontroller.core.util.SingletonTask;
import net.floodlightcontroller.tarn.events.HostChangeEvent;
import net.floodlightcontroller.tarn.web.HostSerializer;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by geddingsbarrineau on 8/24/17.
 * 
 */
@JsonSerialize(using = HostSerializer.class)
public class Host {
    private static final Logger log = LoggerFactory.getLogger(Host.class);
    
    private final IPv4Address internal;
    private IPv4Address external;
    private final int memberAS;
    
    private final AddressGenerator addressGenerator;
    private final DwellTimeGenerator dwellTimeGenerator;
    
    private final SingletonTask changeAddressTask;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @JsonCreator
    public Host(@JsonProperty("internal-address") String internal, 
                @JsonProperty("member-as") int memberAS) {
        this.internal = IPv4Address.of(internal);
        this.external = IPv4Address.NONE;
        this.memberAS = memberAS;
        this.dwellTimeGenerator = new DwellTimeGenerator();
        this.addressGenerator = new AddressGenerator(this.internal.getInt(), this.dwellTimeGenerator.getTotalUpdates());
        
        changeAddressTask = new SingletonTask(executor, new ChangeAddressRunnable());
        changeAddressTask.reschedule(0, TimeUnit.SECONDS);
    }

    public IPv4Address getInternalAddress() {
        return internal;
    }

    public IPv4Address getExternalAddress() {
        return external;
    }

    public int getMemberAS() {
        return memberAS;
    }
    
    class ChangeAddressRunnable implements Runnable {
        @Override
        public void run() {
            IPv4Address newAddress = addressGenerator.getNextAddress();
            RandomizerService.eventBus.post(new HostChangeEvent(Host.this));
            external = newAddress;
            changeAddressTask.reschedule(dwellTimeGenerator.getNextDwellTime(), TimeUnit.MILLISECONDS);
        }
    }
    
    class AddressGenerator {
        Random rng;

        AddressGenerator(long seed, long totalUpdates) {
            int rate = 5;
            rng = new Random(seed);
//            long totalUpdates = Instant.now().getEpochSecond() / rate;
            for (int i = 0; i < totalUpdates; i++) rng.nextInt();
        }
        
        void catchUp() {
            
        }

        IPv4Address getNextAddress() {
            int next = rng.nextInt();
            return IPv4Address.of(next);
        }
    }
 
    class DwellTimeGenerator {
        List<Double> dwellTimes;
        long totalUpdates = 0;
        int index = 0;
        
        DwellTimeGenerator() {
            IPParser parser = new IPParser("/184.164.240.0-0.5.txt");
            dwellTimes = parser.getDwellTimes();
            catchUp();
        }
        
        void catchUp() {
            double totalSeconds = Instant.now().getEpochSecond();
            double cumulativeSeconds = 0;
            
            int index = 0;
            while (cumulativeSeconds < totalSeconds) {
                cumulativeSeconds += dwellTimes.get(index);
                index++;
                if (index >= dwellTimes.size()) index = 0;
                totalUpdates++;
            }
            this.index = index;
            
            log.info("Cumulative: {}, Total: {}, Index: {}", new Object[]{cumulativeSeconds, totalSeconds, index});
            try {
                Thread.sleep((long)(cumulativeSeconds*1000-totalSeconds*1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long getNextDwellTime() {
            Double dwell = dwellTimes.get(index);
            log.info("New dwell time for host {} index {}: {}", new Object[]{internal, index, dwell});
            index++;
            if (index >= dwellTimes.size()) index = 0;
            totalUpdates++;
            return Math.round(dwell * 1000);
        }

        public long getTotalUpdates() {
            return totalUpdates;
        }
    }
}
