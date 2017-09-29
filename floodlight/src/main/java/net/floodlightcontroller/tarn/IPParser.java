package net.floodlightcontroller.tarn;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geddingsbarrineau on 8/23/17.
 */
public class IPParser {
    private final Logger log = LoggerFactory.getLogger(IPParser.class);
    
    private final String fileName;
    
    private List<IPv4Address> addresses;
    private List<Double> dwellTimes;

    public IPParser(String fileName) {
        this.fileName = fileName;
        addresses = new ArrayList<>();
        dwellTimes = new ArrayList<>();
        parse();
    }

    public List<IPv4Address> getAddresses() {
        return addresses;
    }

    public List<Double> getDwellTimes() {
        return dwellTimes;
    }

    private void parse() {
        parseFromBuffer(new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("tarn/IPList/184.164.240.0-0.1.txt"))));
    }
    
    private void parseFromBuffer(BufferedReader bufferedReader) {
        String line;
        String[] parts;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                parts = line.split(",");
                if (parts.length == 2) {
                    addresses.add(IPv4Address.of(parts[0]));
                    dwellTimes.add(Double.valueOf(parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
