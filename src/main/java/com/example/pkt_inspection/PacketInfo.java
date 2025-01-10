package com.example.pkt_inspection;
import org.pcap4j.packet.Packet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PacketInfo {
    private Packet packet;
    private LocalDateTime timestamp;
    private String sourceIP;
    private String destinationIP;
    private int sourcePort;
    private int destinationPort;
    private String protocol;
    private int size;

    public PacketInfo(Packet packet, LocalDateTime timestamp, String sourceIP,
                      String destinationIP, int sourcePort, int destinationPort,
                      String protocol, int size) {
        this.packet = packet;
        this.timestamp = timestamp;
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.protocol = protocol;
        this.size = size;
    }

    // Getters
    public Packet getPacket() { return packet; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSourceIP() { return sourceIP; }
    public String getDestinationIP() { return destinationIP; }
    public int getSourcePort() { return sourcePort; }
    public int getDestinationPort() { return destinationPort; }
    public String getProtocol() { return protocol; }
    public int getSize() { return size; }

    public String getDetailedInfo() {
        StringBuilder details = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        
        details.append("Packet Details:\n");
        details.append("==============\n\n");
        
        details.append(String.format("Timestamp: %s\n", timestamp.format(formatter)));
        details.append(String.format("Protocol: %s\n", protocol));
        details.append(String.format("Size: %d bytes\n\n", size));
        
        details.append("Source:\n");
        details.append(String.format("  IP: %s\n", sourceIP));
        details.append(String.format("  Port: %d\n\n", sourcePort));
        
        details.append("Destination:\n");
        details.append(String.format("  IP: %s\n", destinationIP));
        details.append(String.format("  Port: %d\n\n", destinationPort));
        
        if (packet != null) {
            details.append("Raw Packet Data:\n");
            details.append("---------------\n");
            details.append(packet.toString());
        }
        
        return details.toString();
    }
}