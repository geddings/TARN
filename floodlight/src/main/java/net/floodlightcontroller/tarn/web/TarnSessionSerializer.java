package net.floodlightcontroller.tarn.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.floodlightcontroller.tarn.TarnSession;
import org.projectfloodlight.openflow.types.IpProtocol;

import java.io.IOException;

/**
 * @author Geddings Barrineau, geddings.barrineau@bigswitch.com on 2/26/18.
 */
public class TarnSessionSerializer extends JsonSerializer<TarnSession> {

    @Override
    public void serialize(TarnSession session, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();

        jgen.writeStringField("id", session.getId().toString());
        jgen.writeStringField("status", session.getStatus().name());
        jgen.writeStringField("direction", session.getDirection().name());

        String protocolName = "";
        if (session.getIpProtocol() == IpProtocol.TCP) protocolName = "TCP";
        else if (session.getIpProtocol() == IpProtocol.UDP) protocolName = "UDP";
        else if (session.getIpProtocol() == IpProtocol.ICMP) protocolName = "ICMP";
        else if (session.getIpProtocol() == IpProtocol.IPv6_ICMP) protocolName = "ICMP6";
        jgen.writeStringField("ip-protocol", protocolName);

        jgen.writeStringField("in-port", session.getInPort().toString());
        jgen.writeStringField("out-port", session.getOutPort().toString());
        jgen.writeStringField("internal-src-ip", session.getInternalSrcIp().toString());
        jgen.writeStringField("internal-dst-ip", session.getInternalDstIp().toString());
        jgen.writeStringField("external-src-ip", session.getExternalSrcIp().toString());
        jgen.writeStringField("external-dst-ip", session.getExternalDstIp().toString());

        if (session.hasTransportPorts()) {
            jgen.writeStringField("internal-src-port", session.getInternalSrcPort().toString());
            jgen.writeStringField("internal-dst-port", session.getInternalDstPort().toString());
            jgen.writeStringField("external-src-port", session.getExternalSrcPort().toString());
            jgen.writeStringField("external-dst-port", session.getExternalDstPort().toString());
        }

        jgen.writeEndObject();
    }
}
