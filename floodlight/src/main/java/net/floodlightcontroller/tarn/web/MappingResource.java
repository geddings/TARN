package net.floodlightcontroller.tarn.web;

import java.io.IOException;
import java.util.Optional;

import org.projectfloodlight.openflow.types.IPv4Address;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.floodlightcontroller.tarn.PrefixMapping;
import net.floodlightcontroller.tarn.TarnService;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/21/18.
 */
public class MappingResource extends ServerResource {

    @Get
    public Object getPrefixMapping() {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());
        String internalIp = (String) getRequestAttributes().get("internal-ip");

        IPv4Address iPv4Address;
        try {
            iPv4Address = IPv4Address.of(internalIp);
        } catch (Exception e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid internal IP address given.");
            return null;
        }
        Optional<PrefixMapping> mapping = tarnService.getPrefixMapping(iPv4Address);

        if (mapping.isPresent()) {
            return mapping.get();
        } else {
            setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Prefix mapping not found.");
            return null;
        }
    }

    @Put
    @Post
    public Object updatePrefixMapping(String json) throws IOException {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());

        if (json == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Must provide an internal-ip and external-prefix for a prefix mapping to be added.");
            return null;
        }

        PrefixMapping mapping = new ObjectMapper()
                .reader(PrefixMapping.class)
                .readValue(json);
        tarnService.addPrefixMapping(mapping);

        setStatus(Status.SUCCESS_CREATED, "Prefix mapping successfully created.");
        setLocationRef(getReference().toString() + "/" + mapping.getInternalIp());
        return mapping;
    }

    @Delete
    public Object removePrefixMapping() throws IOException {
        TarnService tarnService = (TarnService) getContext().getAttributes().get(TarnService.class.getCanonicalName());
        String internalIp = (String) getRequestAttributes().get("internal-ip");

        try {
            IPv4Address iPv4Address = IPv4Address.of(internalIp);
            Optional<PrefixMapping> prefixMapping = tarnService.getPrefixMapping(iPv4Address);
            if (!prefixMapping.isPresent()) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Prefix mapping not found.");
                return null;
            }
            tarnService.removePrefixMapping(iPv4Address);
            return ImmutableMap.of("deleted", internalIp);
        } catch (IllegalArgumentException e) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid internal IP address.");
            return null;
        }
    }
}