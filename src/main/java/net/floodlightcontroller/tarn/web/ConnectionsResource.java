package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import net.floodlightcontroller.tarn.Connection;
import net.floodlightcontroller.tarn.IRandomizerService;
import net.floodlightcontroller.tarn.RandomizedHost;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by geddingsbarrineau on 11/2/16.
 */
public class ConnectionsResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(InfoResource.class);

    @Get
    public Object getConnections() {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        return randomizerService.getConnections();
    }

    @Put
    @Post
    public Map<String, String> addConnection(String json) {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        Map<String, String> ret = new HashMap<>();
        Connection connection = parseConnectionFromJson(json, randomizerService);

        if (connection == null) {
            ret.put("ERROR", "Could not parse JSON");
        } else {
            randomizerService.addConnection(connection);
            ret.put("SUCCESS", "Connection added");
        }
        
        return ret;
    }

    /**
     * Expect JSON:
     * {
     * "server"	    :	"valid-ip-address",
     * "switch"		:	"datapath ID",
     * }
     *
     * @param json
     * @return
     */
    protected static final String STR_SERVER = "server";
    protected static final String STR_SWITCH = "switch";

    private static Connection parseConnectionFromJson(String json, IRandomizerService randomizerService) {

        MappingJsonFactory f = new MappingJsonFactory();
        JsonParser jp;

        IPv4Address serverIp = IPv4Address.NONE;
        DatapathId swId = DatapathId.NONE;
        RandomizedHost randomizedHost;

        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            try {
                jp = f.createParser(json);
            } catch (JsonParseException e) {
                throw new IOException(e);
            }

            jp.nextToken();
            if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected START_OBJECT");
            }

            while (jp.nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentToken() != JsonToken.FIELD_NAME) {
                    throw new IOException("Expected FIELD_NAME");
                }

                String key = jp.getCurrentName().toLowerCase().trim();
                jp.nextToken();
                String value = jp.getText().toLowerCase().trim();
                if (value.isEmpty() || key.isEmpty()) {
                    continue;
                } else if (key.equals(STR_SERVER)) {
                    try {
                        serverIp = IPv4Address.of(value);
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid IPv4 address {}", value);
                    }
                } else if (key.equals(STR_SWITCH)) {
                    try {
                        swId = DatapathId.of(value);
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid prefix {}", value);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error parsing JSON into RandomizedHost {}", e);
        }

        randomizedHost = randomizerService.getServer(serverIp);

        if (!serverIp.equals(IPv4Address.NONE)
                && !swId.equals(DatapathId.NONE)
                && randomizedHost != null) {
            return new Connection(null, null, null, null, null);
        } else {
            return null;
        }
    }
}