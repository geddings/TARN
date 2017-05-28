package net.floodlightcontroller.randomizer.web;

import net.floodlightcontroller.randomizer.IRandomizerService;
import net.floodlightcontroller.randomizer.IRandomizerService.RandomizerReturnCode;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by geddingsbarrineau on 9/21/16.
 */
public class ModuleResource extends ServerResource {
    protected static Logger log = LoggerFactory.getLogger(ModuleResource.class);
    protected static final String STR_OPERATION_ENABLE = "enable";
    protected static final String STR_OPERATION_DISABLE = "disable";

    @Get
    public Map<String, String> getModuleMode() {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());

        Map<String, String> ret = new HashMap<String, String>();
        ret.put("enabled", Boolean.toString(randomizerService.isEnabled()));
        return ret;
    }

    @Put
    @Post
    public Map<String, String> handleModule(String json) {
        IRandomizerService randomizerService = (IRandomizerService) getContext().getAttributes().get(IRandomizerService.class.getCanonicalName());
        String operation = (String) getRequestAttributes().get("operation");

        Map<String, String> ret = new HashMap<String, String>();

        if (operation.equalsIgnoreCase(STR_OPERATION_ENABLE)) {
            RandomizerReturnCode rc = randomizerService.enable();
            switch (rc) {
                case ENABLED:
                    ret.put(Code.CODE, Code.OKAY);
                    ret.put(Code.MESSAGE, "Randomizer enabled.");
                    break;
                default:
                    ret.put(Code.CODE, Code.ERR_BAD_ERR_CODE);
                    ret.put(Code.MESSAGE, "Error: Unexpected error code " + rc.toString());
                    break;
            }
        } else if (operation.equalsIgnoreCase(STR_OPERATION_DISABLE)) {
            RandomizerReturnCode rc = randomizerService.disable();
            switch (rc) {
                case DISABLED:
                    ret.put(Code.CODE, Code.OKAY);
                    ret.put(Code.MESSAGE, "Randomizer disabled.");
                    break;
                default:
                    ret.put(Code.CODE, Code.ERR_BAD_ERR_CODE);
                    ret.put(Code.MESSAGE, "Error: Unexpected error code " + rc.toString());
                    break;
            }
        } else {
            ret.put(Code.CODE, Code.ERR_UNDEF_OPERATION);
            ret.put(Code.MESSAGE, "Error: Undefined operation " + operation);
        }

        return ret;
    }
}
