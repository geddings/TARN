package net.floodlightcontroller.packet;

import org.projectfloodlight.openflow.types.IPv4Address;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    @Override
    public byte[] serialize() {
        int length = 12 + queries.stream().mapToInt(Query::length).sum() + answers.stream().mapToInt(Answer::length).sum();
        byte[] data = new byte[length];
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        bb.write(transactionId.byteValue());
        bb.write(flags.byteValue());
        bb.write(questions.byteValue());
        bb.write(answerRRs.byteValue());
        bb.write(authorityRRs.byteValue());
        bb.write(additionalRRs.byteValue());
        queries.forEach(q -> {
            domainByteLocations.put(q.queryDomainName, bb.size());
            try {
                bb.write(q.serialize());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (isResponse()) {
            answers.forEach(a -> {
                try {
                    bb.write(a.serialize(domainByteLocations));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            authorities.forEach(a -> {
                try {
                    bb.write(a.serialize(domainByteLocations));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            additionals.forEach(a -> {
                try {
                    bb.write(a.serialize(domainByteLocations));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        return bb.toByteArray();
    }

    @Override
    public IPacket deserialize(byte[] data, int offset, int length) throws PacketParsingException {
        return null;
    }

    public static class Query {
        String queryDomainName;
        short queryType;
        short queryClass;

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
            byte[] data = new byte[length()];
            ByteArrayOutputStream bb = new ByteArrayOutputStream();
            if (domainByteLocations.containsKey(name)) {
                Short val = (short) (domainByteLocations.get(name).byteValue() | 0xc000);
                bb.write(val.byteValue());
            } else {
                String[] labels = name.split(Pattern.quote("."));
                for (String label : labels) {
                    bb.write((byte) label.length());
                    try {
                        bb.write(label.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                bb.write((byte) 0);
            }
            bb.write(rrType.value.byteValue());
            bb.write(rrClass.value.byteValue());
            bb.write(ttl);
            bb.write(Short.valueOf(rdLength).byteValue());

            switch (rrType) {
                case A:
                    try {
                        bb.write(IPv4Address.of(rData).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                case NS:
                    try {
                        bb.write(nameserverToBytes(rData, domainByteLocations));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            return bb.toByteArray();
        }
        
        private byte[] nameserverToBytes(String ns, Map<String, Integer> domainByteLocations) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            
            while (!ns.isEmpty()) {
                if (domainByteLocations.containsKey(ns)) {
                    System.out.println("Writing: " + String.format("0x%04x", (0xc000 | domainByteLocations.get(ns))));
                    Short pointer = (short) (0xc000 | domainByteLocations.get(ns));
                    bout.write(pointer.byteValue());
                    ns = "";
                } else {
                    if (ns.contains(".")) {
                        String[] labels = ns.split(Pattern.quote("."), 2);
                        System.out.println("Writing: " + labels[0]);
                        bout.write((byte) labels[0].length());
                        try {
                            bout.write(labels[0].getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ns = labels[1];
                    } else {
                        System.out.println("Writing: " + ns);
                        try {
                            bout.write(ns.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ns = "";
                    }
                }
            }
            return bout.toByteArray();
        }
    }

    public static class Answer extends ResourceRecord {
//        String answerDomainName;
//        short answerType;
//        short answerClass;
//        int attl;
//        short dataLength;
//        byte[] address;

        public Answer(String answerDomainName, RRTYPE answerType, RRCLASS answerClass, int ttl, short dataLength, String address) {
//            this.answerDomainName = answerDomainName;
//            this.answerType = answerType;
//            this.answerClass = answerClass;
//            this.ttl = ttl;
//            this.dataLength = dataLength;
//            this.address = address;
            super(answerDomainName, answerType, answerClass, ttl, dataLength, address);
        }
//        public void setAnswerDomainName(String answerDomainName) {
//            this.answerDomainName = answerDomainName;
//        }
//
//        public void setAnswerType(short answerType) {
//            this.answerType = answerType;
//        }
//
//        public void setAnswerClass(short answerClass) {
//            this.answerClass = answerClass;
//        }
//
//        public void setTtl(int ttl) {
//            this.ttl = ttl;
//        }
//
//        public void setDataLength(short dataLength) {
//            this.dataLength = dataLength;
//        }
//
//        public void setAddress(byte[] address) {
//            this.address = address;
//        }

//        private int length() {
//            return answerDomainName.getBytes().length + 10 + address.length;
//        }
//
//        private byte[] serialize() {
//            byte[] data = new byte[length()];
//            ByteBuffer bb = ByteBuffer.wrap(data);
//            bb.put(answerDomainName.getBytes());
//            bb.putShort(answerType);
//            bb.putShort(answerClass);
//            bb.putInt(ttl);
//            bb.putShort(dataLength);
//            bb.put(address);
//            return data;
//        }
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

        RRTYPE(int value) {
            this.value = (short) value;
        }
    }
    
    public enum RRCLASS {
        IN(1),
        CS(2),
        CH(3),
        HS(4);
        
        private final Short value;
        
        RRCLASS(int value) {
            this.value = (short) value;
        }
    }
}
