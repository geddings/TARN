package net.floodlightcontroller.packet;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.projectfloodlight.openflow.types.IPv4Address;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by @geddings on 11/9/17.
 */
public class DNS extends BasePacket {

    private Short transactionId = 0;
    private Short flags = 0x0100;
    private Short questions = 0;
    private Short answerRRs = 0;
    private Short authorityRRs = 0;
    private Short additionalRRs = 0;

    private List<Query> queries;
    private List<Answer> answers;
    private List<Authority> authorities;
    private List<Additional> additionals;

    private Map<String, Integer> domainByteLocations;

    public DNS() {
        queries = new ArrayList<>();
        answers = new ArrayList<>();
        authorities = new ArrayList<>();
        additionals = new ArrayList<>();
        domainByteLocations = new HashMap<>();
    }

    public DNS setTransactionId(short transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    private boolean isQuery() {
        return (((flags >>> 15) & 1) == 0);
    }

    private boolean isResponse() {
        return (((flags >>> 15) & 1) != 0);
    }

    public DNS setFlags(short flags) {
        this.flags = flags;
        return this;
    }

    public DNS setQuestions(int questions) {
        this.questions = (short) questions;
        return this;
    }

    public DNS setAnswerRRs(int answerRRs) {
        this.answerRRs = (short) answerRRs;
        return this;
    }

    public DNS setAuthorityRRs(int authorityRRs) {
        this.authorityRRs = (short) authorityRRs;
        return this;
    }

    public DNS setAdditionalRRs(int additionalRRs) {
        this.additionalRRs = (short) additionalRRs;
        return this;
    }

    public DNS addQuery(Query query) {
        queries.add(query);
        return this;
    }

    public DNS addAnswer(Answer answer) {
        answers.add(answer);
        return this;
    }

    public DNS addAuthority(Authority authority) {
        authorities.add(authority);
        return this;
    }

    public DNS addAdditional(Additional additional) {
        additionals.add(additional);
        return this;
    }

    public Short getTransactionId() {
        return transactionId;
    }

    public Short getFlags() {
        return flags;
    }

    public Short getQuestions() {
        return questions;
    }

    public Short getAnswerRRs() {
        return answerRRs;
    }

    public Short getAuthorityRRs() {
        return authorityRRs;
    }

    public Short getAdditionalRRs() {
        return additionalRRs;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public List<Additional> getAdditionals() {
        return additionals;
    }

    @Override
    public byte[] serialize() {
        int packetSize = 0;
        ByteArrayDataOutput bado = ByteStreams.newDataOutput();
        bado.writeShort(transactionId);
        bado.writeShort(flags);
        bado.writeShort(questions);
        bado.writeShort(answerRRs);
        bado.writeShort(authorityRRs);
        bado.writeShort(additionalRRs);

        packetSize += 12;

        for (Query query : queries) {
            domainByteLocations.put(query.queryDomainName, packetSize);
            bado.write(query.serialize());
            packetSize += query.length();
        }

        if (isResponse()) {
            for (Answer answer : answers) {
                bado.write(answer.serialize(domainByteLocations));
            }
            for (Authority authority : authorities) {
                bado.write(authority.serialize(domainByteLocations));
            }
            for (Additional additional : additionals) {
                bado.write(additional.serialize(domainByteLocations));
            }
        }

        return bado.toByteArray();
    }

    @Override
    public IPacket deserialize(byte[] data, int offset, int length) throws PacketParsingException {
        return deserialize(data, offset);
    }

    public IPacket deserialize(byte[] data, int start) throws PacketParsingException {
        ByteArrayDataInput badi = ByteStreams.newDataInput(data, start);
        this.transactionId = badi.readShort();
        this.flags = badi.readShort();
        this.questions = badi.readShort();
        this.answerRRs = badi.readShort();
        this.authorityRRs = badi.readShort();
        this.additionalRRs = badi.readShort();

        /* Deserialize questions */
        for (int numQuestions = 0; numQuestions < questions; numQuestions++) {
            boolean cont = true;
            StringBuilder sb = new StringBuilder();
            int chars = (int) badi.readByte();
            while (cont) {
                for (int i = 0; i < chars; i++) {
                    sb.append((char) badi.readByte());
                }
                chars = (int) badi.readByte();
                if (chars == 0) {
                    cont = false;
                } else {
                    sb.append(".");
                }
            }
            Query query = new Query(sb.toString(), badi.readShort(), badi.readShort());
            queries.add(query);
        }

        /* Deserialize answers */
        for (int numAnswers = 0; numAnswers < answerRRs; numAnswers++) {
            String resourceName = "";
            byte initial = badi.readByte();
            // Check for pointer
            if ((initial & 0xc0) != 0) {
                short pointer = 0;
                byte secondary = badi.readByte();
                initial = (byte) (initial & 0x0F);
                pointer |= initial;
                pointer = (short) (pointer << 8);
                pointer |= secondary;
                // Get question number
                pointer = (short) (pointer - 12);
                for (int i = 0; i < questions; i++) {
                    if (pointer - queries.get(i).length() < 0) {
                        resourceName = queries.get(i).queryDomainName;
                        break;
                    }
                }
            } else {
                // TODO: Handle case where pointer is not used
            }
            RRTYPE rrType = RRTYPE.valueOf(badi.readShort());
            RRCLASS rrClass = RRCLASS.valueOf(badi.readShort());
            int ttl = badi.readInt();
            short rdLength = badi.readShort();
            String rData = IPv4Address.of(badi.readInt()).toString();
            Answer answer = new Answer(resourceName, rrType, rrClass, ttl, rdLength, rData);
            answers.add(answer);
        }

        return this;
    }

    public static class Query {
        public String queryDomainName;
        public short queryType;
        public short queryClass;

        public Query(String queryDomainName, short queryType, short queryClass) {
            this.queryDomainName = queryDomainName;
            this.queryType = queryType;
            this.queryClass = queryClass;
        }

        public Query(String queryDomainName) {
            this.queryDomainName = queryDomainName;
            this.queryType = 1;
            this.queryClass = 1;
        }

        private int length() {
            String[] labels = queryDomainName.split(Pattern.quote("."));
            return Arrays.stream(labels).mapToInt(String::length).sum() + labels.length + 5; // the 5 bytes are the null character (1), the type (2), and the class (2)
        }

        private byte[] serialize() {
            byte[] data = new byte[length()];
            ByteBuffer bb = ByteBuffer.wrap(data);
            String[] labels = queryDomainName.split(Pattern.quote("."));
            for (String label : labels) {
                bb.put((byte) label.length());
                bb.put(label.getBytes());
            }
            bb.put((byte) 0);
            bb.putShort(queryType);
            bb.putShort(queryClass);
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Query query = (Query) o;

            if (queryType != query.queryType) return false;
            if (queryClass != query.queryClass) return false;
            return queryDomainName != null ? queryDomainName.equals(query.queryDomainName) : query.queryDomainName == null;
        }

        @Override
        public int hashCode() {
            int result = queryDomainName != null ? queryDomainName.hashCode() : 0;
            result = 31 * result + (int) queryType;
            result = 31 * result + (int) queryClass;
            return result;
        }


        @Override
        public String toString() {
            return "Query{" +
                    "queryDomainName='" + queryDomainName + '\'' +
                    ", queryType=" + queryType +
                    ", queryClass=" + queryClass +
                    '}';
        }
    }

    private static class ResourceRecord {
        String name;
        RRTYPE rrType;
        RRCLASS rrClass;
        int ttl;
        short rdLength;
        String rData;

        ResourceRecord(String name, RRTYPE rrType, RRCLASS rrClass, int ttl, short rdLength, String rData) {
            this.name = name;
            this.rrType = rrType;
            this.rrClass = rrClass;
            this.ttl = ttl;
            this.rdLength = rdLength;
            this.rData = rData;
        }

        int length() {
            String[] labels = name.split(Pattern.quote("."));
            return Arrays.stream(labels).mapToInt(String::length).sum() + labels.length + 1 + 10 + rdLength;
        }

        byte[] serialize(Map<String, Integer> domainByteLocations) {
            ByteArrayDataOutput bado = ByteStreams.newDataOutput();

            if (domainByteLocations.containsKey(name)) {
                Short val = (short) (domainByteLocations.get(name).byteValue() | 0xc000);
                bado.writeShort(val);
            } else {
                String[] labels = name.split(Pattern.quote("."));
                for (String label : labels) {
                    bado.writeByte(label.length());
                    bado.writeChars(label);
                }
                bado.writeByte(0);
            }
            bado.writeShort(rrType.value.byteValue());
            bado.writeShort(rrClass.value.byteValue());
            bado.writeInt(ttl);
            bado.writeShort(rdLength);

            switch (rrType) {
                case A:
                    bado.write(IPv4Address.of(rData).getBytes());
                    break;
                case NS:
                    bado.write(nameserverToBytes(rData, domainByteLocations));
                    break;
                default:
                    break;
            }
            return bado.toByteArray();
        }

        private byte[] nameserverToBytes(String ns, Map<String, Integer> domainByteLocations) {
            ByteArrayDataOutput bado = ByteStreams.newDataOutput();

            while (!ns.isEmpty()) {
                if (domainByteLocations.containsKey(ns)) {
                    Short pointer = (short) (0xc000 | domainByteLocations.get(ns));
                    bado.writeShort(pointer);
                    ns = "";
                } else {
                    if (ns.contains(".")) {
                        String[] labels = ns.split(Pattern.quote("."), 2);
                        bado.writeByte(labels[0].length());
                        bado.writeChars(labels[0]);
                        ns = labels[1];
                    } else {
                        bado.writeChars(ns);
                        ns = "";
                    }
                }
            }
            return bado.toByteArray();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResourceRecord that = (ResourceRecord) o;

            if (ttl != that.ttl) return false;
            if (rdLength != that.rdLength) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (rrType != that.rrType) return false;
            if (rrClass != that.rrClass) return false;
            return rData != null ? rData.equals(that.rData) : that.rData == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (rrType != null ? rrType.hashCode() : 0);
            result = 31 * result + (rrClass != null ? rrClass.hashCode() : 0);
            result = 31 * result + ttl;
            result = 31 * result + (int) rdLength;
            result = 31 * result + (rData != null ? rData.hashCode() : 0);
            return result;
        }


        @Override
        public String toString() {
            return "ResourceRecord{" +
                    "name='" + name + '\'' +
                    ", rrType=" + rrType +
                    ", rrClass=" + rrClass +
                    ", ttl=" + ttl +
                    ", rdLength=" + rdLength +
                    ", rData='" + rData + '\'' +
                    '}';
        }
    }

    public static class Answer extends ResourceRecord {
        public Answer(String answerDomainName, RRTYPE answerType, RRCLASS answerClass, int ttl, short dataLength, String address) {
            super(answerDomainName, answerType, answerClass, ttl, dataLength, address);
        }
    }

    public static class Authority extends ResourceRecord {
        public Authority(String name, RRTYPE rrType, RRCLASS rrClass, int ttl, short rdLength, String rData) {
            super(name, rrType, rrClass, ttl, rdLength, rData);
        }
    }

    public static class Additional extends ResourceRecord {
        public Additional(String name, RRTYPE rrType, RRCLASS rrClass, int ttl, short rdLength, String rData) {
            super(name, rrType, rrClass, ttl, rdLength, rData);
        }
    }

    public enum RRTYPE {
        A(1),
        NS(2),
        MD(3),
        MF(4),
        CNAME(5),
        SOA(6),
        MB(7),
        MG(8),
        MR(9),
        NULL(10),
        WKS(11),
        PTR(12),
        HINFO(13),
        MINFO(14),
        MX(15),
        TXT(16);

        private final Short value;

        private static Map<Short, RRTYPE> map = new HashMap<>();

        static {
            for (RRTYPE type : RRTYPE.values()) {
                map.put(type.value, type);
            }
        }

        RRTYPE(int value) {
            this.value = (short) value;
        }

        public static RRTYPE valueOf(short value) {
            return map.get(value);
        }
    }

    public enum RRCLASS {
        IN(1),
        CS(2),
        CH(3),
        HS(4);

        private final Short value;

        private static Map<Short, RRCLASS> map = new HashMap<>();

        static {
            for (RRCLASS rrclass : RRCLASS.values()) {
                map.put(rrclass.value, rrclass);
            }
        }

        RRCLASS(int value) {
            this.value = (short) value;
        }

        public static RRCLASS valueOf(short value) {
            return map.get(value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DNS dns = (DNS) o;

        if (transactionId != null ? !transactionId.equals(dns.transactionId) : dns.transactionId != null) return false;
        if (flags != null ? !flags.equals(dns.flags) : dns.flags != null) return false;
        if (questions != null ? !questions.equals(dns.questions) : dns.questions != null) return false;
        if (answerRRs != null ? !answerRRs.equals(dns.answerRRs) : dns.answerRRs != null) return false;
        if (authorityRRs != null ? !authorityRRs.equals(dns.authorityRRs) : dns.authorityRRs != null) return false;
        if (additionalRRs != null ? !additionalRRs.equals(dns.additionalRRs) : dns.additionalRRs != null) return false;
        if (queries != null ? !queries.equals(dns.queries) : dns.queries != null) return false;
        if (answers != null ? !answers.equals(dns.answers) : dns.answers != null) return false;
        if (authorities != null ? !authorities.equals(dns.authorities) : dns.authorities != null) return false;
        return additionals != null ? additionals.equals(dns.additionals) : dns.additionals == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (transactionId != null ? transactionId.hashCode() : 0);
        result = 31 * result + (flags != null ? flags.hashCode() : 0);
        result = 31 * result + (questions != null ? questions.hashCode() : 0);
        result = 31 * result + (answerRRs != null ? answerRRs.hashCode() : 0);
        result = 31 * result + (authorityRRs != null ? authorityRRs.hashCode() : 0);
        result = 31 * result + (additionalRRs != null ? additionalRRs.hashCode() : 0);
        result = 31 * result + (queries != null ? queries.hashCode() : 0);
        result = 31 * result + (answers != null ? answers.hashCode() : 0);
        result = 31 * result + (authorities != null ? authorities.hashCode() : 0);
        result = 31 * result + (additionals != null ? additionals.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "DNS{" +
                "transactionId=" + transactionId +
                ", flags=" + flags +
                ", questions=" + questions +
                ", answerRRs=" + answerRRs +
                ", authorityRRs=" + authorityRRs +
                ", additionalRRs=" + additionalRRs +
                ", queries=" + queries +
                ", answers=" + answers +
                ", authorities=" + authorities +
                ", additionals=" + additionals +
                '}';
    }
}
