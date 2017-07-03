package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import net.floodlightcontroller.tarn.IRandomizerService;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by geddingsbarrineau on 9/21/16.
 */
public class ConfigResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(ConfigResource.class);
    protected static final String STR_SETTING_RANDOMIZE = "randomize";
    protected static final String STR_SETTING_LOCALPORT = "localport";
    protected static final String STR_SETTING_WANPORT = "wanport";

    protected static final String STR_INVALID_KEY = "invalid-key";
    protected static final String STR_INVALID_VALUE = "invalid-value";

//    @Get
//    public Map<String, String> getConfiguration() {
//        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
//
//        Map<String, String> ret = new HashMap<String, String>();
//        ret.put(STR_SETTING_RANDOMIZE, Boolean.toString(randomizerService.isRandom()));
//        ret.put(STR_SETTING_LOCALPORT, randomizerService.getLanPort().toString());
//        ret.put(STR_SETTING_WANPORT, randomizerService.getWanPort().toString());
//        return ret;
//    }
//
//    @Put
//    @Post
//    public Map<String, String> handleConfig(String json) {
//        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
//        Map<String, String> ret = new HashMap<String, String>();
//        Map<String, Object> config = parseConfigFromJson(json);
//
//        for (String key : config.keySet()) {
//            RandomizerReturnCode rc;
//            switch (key) {
//                case STR_SETTING_RANDOMIZE:
//                    rc = randomizerService.setRandom((Boolean) config.get(key));
//                    switch (rc) {
//                        case CONFIG_SET:
//                            ret.put(Code.CODE, Code.OKAY);
//                            ret.put(Code.MESSAGE, "Randomize set to " + config.get(key));
//                            break;
//                        default:
//                            ret.put(Code.CODE, Code.ERR_BAD_ERR_CODE);
//                            ret.put(Code.MESSAGE, "Error: Unexpected error code " + rc.toString());
//                            break;
//                    }
//                    break;
//                case STR_SETTING_LOCALPORT:
//                    rc = randomizerService.setLanPort((Integer) config.get(key));
//                    switch (rc) {
//                        case CONFIG_SET:
//                            ret.put(Code.CODE, Code.OKAY);
//                            ret.put(Code.MESSAGE, "Local port set to " + config.get(key));
//                            break;
//                        default:
//                            ret.put(Code.CODE, Code.ERR_BAD_ERR_CODE);
//                            ret.put(Code.MESSAGE, "Error: Unexpected error code " + rc.toString());
//                            break;
//                    }
//                    break;
//                case STR_SETTING_WANPORT:
//                    rc = randomizerService.setWanPort((Integer) config.get(key));
//                    switch (rc) {
//                        case CONFIG_SET:
//                            ret.put(Code.CODE, Code.OKAY);
//                            ret.put(Code.MESSAGE, "Wan port set to " + config.get(key));
//                            break;
//                        default:
//                            ret.put(Code.CODE, Code.ERR_BAD_ERR_CODE);
//                            ret.put(Code.MESSAGE, "Error: Unexpected error code " + rc.toString());
//                            break;
//                    }
//                    break;
//                case STR_INVALID_KEY:
//                case STR_INVALID_VALUE:
//                    ret.put(key, (String) config.get(key));
//                    break;
//            }
//        }
//
//        return ret;
//    }

    /**
     * Expect JSON (any of):
     * {
     * 		"randomize"	    :	"boolean",
     * 		"localport"		:	"number",
     * 		"wanport"		:	"number",
     * }
     *
     * @param json
     * @return
     */
    private static Map<String, Object> parseConfigFromJson(String json) {
        MappingJsonFactory f = new MappingJsonFactory();
        JsonParser jp;

        if (json == null || json.isEmpty()) {
            return null;
        }

        Map<String, Object> ret = new HashMap<String, Object>();

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
                }
                switch (key) {
                    case STR_SETTING_RANDOMIZE:
                        try {
                            ret.put(STR_SETTING_RANDOMIZE, Boolean.parseBoolean(value));
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid tarn {}", value);
                            ret.put(STR_INVALID_VALUE, "Invalid value '" + value + "' for key '" + key + "'");
                        }
                        break;
                    case STR_SETTING_LOCALPORT:
                        try {
                            ret.put(STR_SETTING_LOCALPORT, Integer.parseInt(value));
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid local port {}", value);
                            ret.put(STR_INVALID_VALUE, "Invalid value '" + value + "' for key '" + key + "'");
                        }
                        break;
                    case STR_SETTING_WANPORT:
                        try {
                            ret.put(STR_SETTING_WANPORT, Integer.parseInt(value));
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid wan port {}", value);
                            ret.put(STR_INVALID_VALUE, "Invalid value '" + value + "' for key '" + key + "'");
                        }
                        break;
                    default:
                        log.warn("Received invalid key {} parsing Randomizer config", key);
                        ret.put(STR_INVALID_KEY, "Invalid key '" + key + "'");
                        break;
                }
            }
        } catch (IOException e) {
            log.error("Error parsing JSON config {}", e);
        }
        return ret;
    }

}
