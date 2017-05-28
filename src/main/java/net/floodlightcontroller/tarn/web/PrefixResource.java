package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import net.floodlightcontroller.tarn.IRandomizerService;
import net.floodlightcontroller.tarn.RandomizedHost;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Created by geddingsbarrineau on 2/1/17.
 */
public class PrefixResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(PrefixResource.class);
    protected static final String STR_CURRENT = "current";
    protected static final String STR_ALL = "all";
    protected static final String STR_OPERATION_ADD = "add";
    protected static final String STR_OPERATION_REMOVE = "remove";
    
    /* TODO: Add more error checking here */

    @Get
    public Object getPrefixes() {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        String operation = (String) getRequestAttributes().get("operation");

        if (operation.equals(STR_CURRENT)) {
            return randomizerService.getCurrentPrefix();
        }

        if (operation.equals(STR_ALL)) {
//            return Collections.singletonMap("all-prefixes", randomizerService.getPrefixes().
//                    .map(IPAddressWithMask::toString)
//                    .collect(Collectors.toList()));
            return randomizerService.getPrefixes().entrySet()
                    .stream()
                    .collect(Collectors.toMap(e -> e.getKey(),
                            e -> e.getValue().toString()));
        }

        return Collections.singletonMap("ERROR", "Unimplemented configuration option");
    }

    @Put
    @Post
    public Object addPrefixes(String json) {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        String operation = (String) getRequestAttributes().get("operation");

        if (operation.equals(STR_OPERATION_ADD)) {
            randomizerService.addPrefix(parseServerFromJson(json), parsePrefixFromJson(json));
            return Collections.singletonMap("SUCCESS", "Prefix added!");
        }

        if (operation.equals(STR_OPERATION_REMOVE)) {
            randomizerService.removePrefix(parseServerFromJson(json), parsePrefixFromJson(json));
            return Collections.singletonMap("SUCCESS", "Prefix removed!");
        }

        return Collections.singletonMap("ERROR", "Unimplemented configuration option");
    }

    /**
     * Expect JSON:
     * {
     * "ip-address"	:	"valid-ip-address",
     * "mask"          :   "valid-ip-address"
     * }
     *
     * @param json
     * @return
     */
    protected static final String STR_IP = "ip-address";
    protected static final String STR_MASK = "mask";
    protected static final String STR_SERVER = "server";

    private static IPv4AddressWithMask parsePrefixFromJson(String json) {
        MappingJsonFactory f = new MappingJsonFactory();
        JsonParser jp;

        IPv4Address ip = IPv4Address.NONE;
        IPv4Address mask = IPv4Address.NO_MASK;

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
                } else if (key.equals(STR_IP)) {
                    try {
                        ip = IPv4Address.of(value);
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid IPv4 address {}", value);
                    }
                } else if (key.equals(STR_MASK)) {
                    try {
                        mask = IPv4Address.of(value);
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid IPv4 address for mask {}", value);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error parsing JSON into RandomizedHost {}", e);
        }

        if (!ip.equals(IPv4Address.NONE)
                && !mask.equals(IPv4Address.NO_MASK)) {
            return IPv4AddressWithMask.of(ip, mask);
        } else {
            return null;
        }
    }

    private static RandomizedHost parseServerFromJson(String json) {
        MappingJsonFactory f = new MappingJsonFactory();
        JsonParser jp;

        IPv4Address ip = IPv4Address.NONE;
        IPv4AddressWithMask prefix = IPv4AddressWithMask.NONE;

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
                        ip = IPv4Address.of(value);
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid IPv4 address {}", value);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error parsing JSON into RandomizedHost {}", e);
        }

        if (!ip.equals(IPv4Address.NONE)) {
            return new RandomizedHost(ip);
        } else {
            return null;
        }
    }
}
